package ru.brikster.chatty;

import cloud.commandframework.Command;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager.ManagerSettings;
import cloud.commandframework.CommandTree.Node;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler.ExceptionType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.brikster.chatty.adventure.NativeBukkitAudienceProvider;
import ru.brikster.chatty.api.ChattyApiImpl;
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.command.CommandSuggestionsProvider;
import ru.brikster.chatty.command.ProxyingCommandHandler;
import ru.brikster.chatty.command.ProxyingCommandSuggestionsProvider;
import ru.brikster.chatty.command.handler.ClearChatCommandHandler;
import ru.brikster.chatty.command.handler.SpyCommandHandler;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.guice.ConfigsLoader;
import ru.brikster.chatty.guice.GeneralGuiceModule;
import ru.brikster.chatty.metrics.MetricsSender;
import ru.brikster.chatty.misc.VanillaListener;
import ru.brikster.chatty.notification.NotificationTicker;
import ru.brikster.chatty.papi.PapiExpansionInstaller;
import ru.brikster.chatty.pm.MsgCommandHandler;
import ru.brikster.chatty.pm.PrivateMessageSuggestionsProvider;
import ru.brikster.chatty.pm.ReplyCommandHandler;
import ru.brikster.chatty.pm.ignore.AddIgnoreCommandHandler;
import ru.brikster.chatty.pm.ignore.IgnoreListCommandHandler;
import ru.brikster.chatty.pm.ignore.RemoveIgnoreCommandHandler;
import ru.brikster.chatty.proxy.ProxyService;
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.EventUtil;
import ru.brikster.chatty.util.PaperUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

    private final Map<String, ProxyingCommandHandler<CommandSender>> proxyingCommandHandlerMap = new ConcurrentHashMap<>();

    private Injector injector;

    private NotificationTicker notificationTicker;
    private BukkitCommandManager<CommandSender> syncCommandManager;
    private BukkitCommandManager<CommandSender> asyncCommandManager;
    private BukkitAudiences audiences;

    private ProxyingCommandSuggestionsProvider<CommandSender> commandSuggestionsProvider;

    @SneakyThrows
    @Override
    public void onEnable() {
        Path dataFolderPath = Chatty.this.getDataFolder().toPath();
        if (Files.exists(dataFolderPath.resolve("config.yml"))) {
            String backupFolderName = "Chatty_old_" + System.currentTimeMillis();
            Files.move(dataFolderPath, dataFolderPath.resolveSibling(backupFolderName));
            getLogger().log(Level.WARNING, "Found legacy \"config.yml\" file in plugin directory. \"Chatty\" folder was renamed to \"{0}\".", backupFolderName);
        }

        logServerCompatibility();
        initialize();
        registerChattyCommand();
    }

    private void registerChattyCommand() throws Exception {
        this.syncCommandManager = new BukkitCommandManager<>(this,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity());

        Builder<CommandSender> chattyBuilder = syncCommandManager.commandBuilder("chatty");

        Command<CommandSender> infoCommand = Command
                .<CommandSender>newBuilder("chatty", CommandMeta.simple().build())
                .handler(handler -> {
                    Component component = MiniMessage.miniMessage().deserialize(
                            "<gold><bold>Chatty</bold></gold> <gray>(v" + getDescription().getVersion() + ")</gray> - chat management system by <green>@Brikster</green>.<newline>" +
                            "Links: <click:open_url:'https://github.com/Brikster/Chatty'><aqua>GitHub</aqua></click><newline>" +
                            "Use <yellow>/chatty reload</yellow> to reload configuration.");

                    injector.getInstance(BukkitAudiences.class)
                            .sender(handler.getSender())
                            .sendMessage(component);
                }).build();

        Command<CommandSender> reloadCommand = chattyBuilder
                .literal("reload")
                .permission("chatty.command.reload")
                .handler(handler -> {
                    try {
                        closeResources();
                        initialize();
                        injector.getInstance(BukkitAudiences.class)
                                .sender(handler.getSender())
                                .sendMessage(injector.getInstance(MessagesConfig.class).getReloadCommandSuccess());
                    } catch (Throwable t) {
                        getLogger().log(Level.SEVERE, "Error while reloading Chatty", t);
                        handler.getSender().sendMessage("§cError while reloading plugin: " + t.getClass().getSimpleName() + ". See console for more details.");
                    }
                })
                .build();

        syncCommandManager
                .command(infoCommand)
                .command(reloadCommand);
    }

    private void initialize() throws Exception {
        if (isUseNativeAdventurePlatform()) {
            getLogger().log(Level.INFO, "Using native Adventure audience provider");
            audiences = new NativeBukkitAudienceProvider();
        } else {
            getLogger().log(Level.INFO, "Using bundled Adventure audience provider");
            audiences = BukkitAudiences.create(this);
        }
        this.audiences = audiences;

        ChattyInitEvent initEvent = new ChattyInitEvent(audiences);
        getServer().getPluginManager().callEvent(initEvent);

        this.injector = Guice.createInjector(new GeneralGuiceModule(
                        Chatty.this,
                        initEvent.getAudienceProvider(),
                        getDataFolder().toPath()));

        injector.injectMembers(new ConfigsLoader());

        EventPriority priority = injector.getInstance(SettingsConfig.class).getListenerPriority();
        if (priority == EventPriority.MONITOR) {
            priority = EventPriority.HIGHEST;
            getLogger().log(Level.WARNING, "Cannot use monitor priority for listener. HIGHEST priority usage will be forced");
        }

        if (PaperUtil.isPaper()) {
            getLogger().info("Paper detected. Using modern AsyncChatEvent listener.");
            try {
                Class<?> modernClass = Class.forName("ru.brikster.chatty.chat.executor.ModernEventExecutor");
                org.bukkit.event.Listener modernListener = (org.bukkit.event.Listener) injector.getInstance(modernClass);
                this.getServer().getPluginManager().registerEvents(modernListener, this);

                Class<? extends org.bukkit.event.Event> asyncChatEventClass = Class.forName("io.papermc.paper.event.player.AsyncChatEvent").asSubclass(org.bukkit.event.Event.class);

                this.getServer().getPluginManager().registerEvent(asyncChatEventClass, modernListener, priority, (listener, event) -> {
                    if (asyncChatEventClass.isInstance(event)) {
                        try {
                            modernClass.getMethod("onChat", asyncChatEventClass).invoke(listener, event);
                        } catch (Exception e) {
                            getLogger().log(Level.SEVERE, "Error invoking ModernEventExecutor.onChat", e);
                        }
                    }
                }, this, true);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to register modern chat listener", e);
            }
        } else {
            LegacyEventExecutor chatListener = injector.getInstance(LegacyEventExecutor.class);
            this.getServer().getPluginManager().registerEvents(chatListener, this);
            this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, chatListener, this, true);
        }

        VanillaListener miscListener = injector.getInstance(VanillaListener.class);
        this.getServer().getPluginManager().registerEvents(miscListener, this);

        this.notificationTicker = injector.getInstance(NotificationTicker.class);
        notificationTicker.startTicking();

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PapiExpansionInstaller.install(injector);
        }

        PmConfig pmConfig = injector.getInstance(PmConfig.class);

        PrivateMessageSuggestionsProvider pmSuggestionsProvider = injector.getInstance(PrivateMessageSuggestionsProvider.class);
        if (commandSuggestionsProvider == null) {
            commandSuggestionsProvider = new ProxyingCommandSuggestionsProvider<>(pmSuggestionsProvider);
        } else {
            commandSuggestionsProvider.setBackendProvider(pmSuggestionsProvider);
        }

        AddIgnoreCommandHandler addIgnoreCommandHandler = injector.getInstance(AddIgnoreCommandHandler.class);
        RemoveIgnoreCommandHandler removeIgnoreCommandHandler = injector.getInstance(RemoveIgnoreCommandHandler.class);
        IgnoreListCommandHandler ignoreListCommandHandler = injector.getInstance(IgnoreListCommandHandler.class);
        registerProxyingHandler("ignore add", addIgnoreCommandHandler);
        registerProxyingHandler("ignore remove", removeIgnoreCommandHandler);
        registerProxyingHandler("ignore list", ignoreListCommandHandler);

        if (pmConfig.isEnable()) {
            MsgCommandHandler msgCommandHandler = injector.getInstance(MsgCommandHandler.class);
            ReplyCommandHandler replyCommandHandler = injector.getInstance(ReplyCommandHandler.class);
            registerProxyingHandler("msg", msgCommandHandler);
            registerProxyingHandler("reply", replyCommandHandler);
        }

        ClearChatCommandHandler clearChatCommandHandler = injector.getInstance(ClearChatCommandHandler.class);
        registerProxyingHandler("clearchat", clearChatCommandHandler);

        SpyCommandHandler spyCommandHandler = injector.getInstance(SpyCommandHandler.class);
        registerProxyingHandler("spy", spyCommandHandler);

        if (this.asyncCommandManager == null) {
            initAsyncCommandManager();
            if (pmConfig.isEnable()) {
                registerPmCommands(commandSuggestionsProvider);
            }
            registerIgnoreCommand(commandSuggestionsProvider);
            registerMiscCommands();
        }

        ChattyApiImpl.updateInstance(new ChattyApiImpl(injector.getInstance(ChatRegistry.class).getChats()));

        MetricsSender metricsSender = new MetricsSender();
        injector.injectMembers(metricsSender);
        metricsSender.run();
    }

    private void closeResources() throws IOException {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
        if (injector != null) {
            injector.getInstance(PlayerDataRepository.class).close();
            injector.getInstance(ProxyService.class).close();
        }
        try {
            EventUtil.unregisterListeners(PlayerJoinEvent.class, this);
            EventUtil.unregisterListeners(PlayerQuitEvent.class, this);
            EventUtil.unregisterListeners(PlayerDeathEvent.class, this);
            EventUtil.unregisterListeners(AsyncPlayerChatEvent.class, this);
            if (PaperUtil.isPaper()) {
                try {
                    Class<? extends org.bukkit.event.Event> asyncChatEventClass = Class.forName("io.papermc.paper.event.player.AsyncChatEvent").asSubclass(org.bukkit.event.Event.class);
                    EventUtil.unregisterListeners(asyncChatEventClass, this);
                } catch (ClassNotFoundException ignored) {}
            }
        } catch (RuntimeException e) {
            getLogger().log(Level.WARNING, "Failed to unregister listeners cleanly", e);
        }
        if (notificationTicker != null) {
            notificationTicker.cancelTicking();
        }
    }

    private void initAsyncCommandManager() throws Exception {
        this.asyncCommandManager = new BukkitCommandManager<>(this,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity());

        MessagesConfig messagesConfig = injector.getInstance(MessagesConfig.class);

        new MinecraftExceptionHandler<CommandSender>()
                .withHandler(ExceptionType.ARGUMENT_PARSING, (e) -> {
                    String argument = ((ArgumentParseException) e).getCause().toString();
                    return messagesConfig.getCmdArgumentParsingError()
                            .replaceText(AdventureUtil.createReplacement("{argument}", argument));
                })
                .withHandler(ExceptionType.INVALID_SYNTAX, (e) -> {
                    String correctSyntax = ((InvalidSyntaxException) e).getCorrectSyntax();
                    return messagesConfig.getCmdUsageError()
                            .replaceText(AdventureUtil.createReplacement("{usage}", "/" + correctSyntax));
                })
                .withHandler(ExceptionType.INVALID_SENDER, (e) -> messagesConfig.getCmdSenderTypeError())
                .withHandler(ExceptionType.NO_PERMISSION, (e) -> messagesConfig.getCmdNoPermissionError())
                .withHandler(ExceptionType.COMMAND_EXECUTION, (e) -> {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    return messagesConfig.getCmdExecutionError();
                })
                .apply(asyncCommandManager, injector.getInstance(BukkitAudiences.class)::sender);

        asyncCommandManager.setSetting(ManagerSettings.ALLOW_UNSAFE_REGISTRATION, true);
    }

    private void registerMiscCommands() {
        var clearChatCommand = asyncCommandManager
                .commandBuilder("clearchat")
                .permission("chatty.command.clearchat")
                .handler(proxyingCommandHandlerMap.get("clearchat"))
                .build();
        asyncCommandManager.command(clearChatCommand);

        var spyCommand = asyncCommandManager
                .commandBuilder("spy")
                .senderType(Player.class)
                .permission("chatty.command.spy")
                .argument(BooleanArgument.<CommandSender>builder("state")
                        .withLiberal(true)
                        .build())
                .handler(proxyingCommandHandlerMap.get("spy"))
                .build();
        asyncCommandManager.command(spyCommand);
    }

    private void registerIgnoreCommand(CommandSuggestionsProvider<CommandSender> pmSuggestionsProvider) {
        Builder<CommandSender> ignoreCommandBuilder = asyncCommandManager
                 .commandBuilder("ignore")
                 .senderType(Player.class)
                 .permission("chatty.command.ignore");

        Command<CommandSender> ignoreAddCommand = ignoreCommandBuilder
                .literal("add")
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .handler(proxyingCommandHandlerMap.get("ignore add"))
                .build();

        Command<CommandSender> ignoreRemoveCommand = ignoreCommandBuilder
                .literal("remove", "rem", "rm", "delete")
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .handler(proxyingCommandHandlerMap.get("ignore remove"))
                .build();

        Command<CommandSender> ignoreListCommand = ignoreCommandBuilder
                .handler(proxyingCommandHandlerMap.get("ignore list"))
                .build();

        asyncCommandManager
                .command(ignoreAddCommand)
                .command(ignoreRemoveCommand)
                .command(ignoreListCommand);
    }

    private void registerPmCommands(CommandSuggestionsProvider<CommandSender> pmSuggestionsProvider) {
        Command<CommandSender> msgCommand = asyncCommandManager.commandBuilder("msg", "message", "m", "w", "pm", "dm")
                .permission("chatty.pm")
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .argument(StringArgument.greedy("message"))
                .handler(proxyingCommandHandlerMap.get("msg"))
                .build();

        Command<CommandSender> replyCommand = asyncCommandManager.commandBuilder("reply", "r")
                .permission("chatty.pm")
                .argument(StringArgument.greedy("message"))
                .handler(proxyingCommandHandlerMap.get("reply"))
                .build();

        asyncCommandManager
                .command(msgCommand)
                .command(replyCommand);
    }

    private void registerProxyingHandler(String commandName, CommandExecutionHandler<CommandSender> executionHandler) {
        proxyingCommandHandlerMap.compute(commandName, (k, v) -> {
            if (v == null) {
                return new ProxyingCommandHandler<>(executionHandler);
            } else {
                v.setBackendHandler(executionHandler);
                return v;
            }
        });
    }

    @Override
    public void onDisable() {
        unregisterAllCommands(syncCommandManager);
        unregisterAllCommands(asyncCommandManager);
        try {
            closeResources();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot gracefully shutdown Chatty", e);
        }
    }

    private static void unregisterAllCommands(BukkitCommandManager<CommandSender> commandManager) {
        if (commandManager == null) {
            return;
        }
        for (Node<CommandArgument<CommandSender, ?>> node : commandManager.commandTree().getRootNodes()) {
            //noinspection DataFlowIssue
            commandManager.deleteRootCommand(node.getValue().getName());
        }
    }

    private static boolean isUseNativeAdventurePlatform() {
        return PaperUtil.isPaper() && PaperUtil.isSupportAdventure();
    }

    private void logServerCompatibility() {
        String targetVersion = BuildConstants.TARGET_MINECRAFT_VERSION;
        String runningVersion = Bukkit.getMinecraftVersion();
        if (targetVersion == null || targetVersion.isBlank()) {
            return;
        }
        if (runningVersion == null || runningVersion.isBlank()) {
            getLogger().log(Level.INFO, "Chatty targets Minecraft {0}.", targetVersion);
            return;
        }

        int comparison = compareMinecraftVersions(runningVersion, targetVersion);
        if (comparison < 0) {
            getLogger().log(Level.WARNING, "Chatty targets Minecraft {0} but the server is {1}. Some features may not work.",
                    new Object[] {targetVersion, runningVersion});
        } else if (comparison > 0) {
            getLogger().log(Level.INFO, "Chatty targets Minecraft {0}. You are running {1}; report issues if you see regressions.",
                    new Object[] {targetVersion, runningVersion});
        } else {
            getLogger().log(Level.INFO, "Chatty targets Minecraft {0}.", targetVersion);
        }
    }

    private static int compareMinecraftVersions(String left, String right) {
        int[] leftParts = parseMinecraftVersion(left);
        int[] rightParts = parseMinecraftVersion(right);
        int max = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < max; i++) {
            int leftValue = i < leftParts.length ? leftParts[i] : 0;
            int rightValue = i < rightParts.length ? rightParts[i] : 0;
            if (leftValue != rightValue) {
                return Integer.compare(leftValue, rightValue);
            }
        }
        return 0;
    }

    private static int[] parseMinecraftVersion(String version) {
        if (version == null) {
            return new int[0];
        }
        String trimmed = version.trim();
        int end = 0;
        while (end < trimmed.length()) {
            char c = trimmed.charAt(end);
            if ((c >= '0' && c <= '9') || c == '.') {
                end++;
            } else {
                break;
            }
        }
        if (end == 0) {
            return new int[0];
        }
        String[] parts = trimmed.substring(0, end).split("\\.");
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                numbers[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {
                numbers[i] = 0;
            }
        }
        return numbers;
    }

}

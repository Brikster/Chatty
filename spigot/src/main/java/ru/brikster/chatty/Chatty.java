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
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.permission.OrPermission;
import cloud.commandframework.permission.Permission;
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
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.brikster.chatty.adventure.NativeBukkitAudienceProvider;
import ru.brikster.chatty.api.ChattyApiImpl;
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.executor.ModernEventExecutor;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.command.ChatCommand;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.command.CommandSuggestionsProvider;
import ru.brikster.chatty.command.ProxyingCommandHandler;
import ru.brikster.chatty.command.ProxyingCommandSuggestionsProvider;
import ru.brikster.chatty.command.handler.BroadcastCommandHandler;
import ru.brikster.chatty.command.handler.ChatCommandHandler;
import ru.brikster.chatty.command.handler.ClearChatCommandHandler;
import ru.brikster.chatty.command.handler.MuteCommandHandler;
import ru.brikster.chatty.command.handler.SpyCommandHandler;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.config.migration.V2ConfigMigrator;
import ru.brikster.chatty.guice.ConfigsLoader;
import ru.brikster.chatty.guice.GeneralGuiceModule;
import ru.brikster.chatty.metrics.MetricsSender;
import ru.brikster.chatty.misc.ChatLogWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

    private final Map<String, ProxyingCommandHandler<CommandSender>> proxyingCommandHandlerMap = new ConcurrentHashMap<>();

    private Injector injector;

    private NotificationTicker notificationTicker;
    private BukkitCommandManager<CommandSender> syncCommandManager;
    private BukkitCommandManager<CommandSender> asyncCommandManager;

    private ProxyingCommandSuggestionsProvider<CommandSender> commandSuggestionsProvider;

    @SneakyThrows
    @Override
    public void onEnable() {
        Path dataFolderPath = Chatty.this.getDataFolder().toPath();

        Map<String, Object> legacyConfig = null;
        String backupFolderName = null;
        if (Files.exists(dataFolderPath.resolve("config.yml"))) {
            backupFolderName = "Chatty_old_" + System.currentTimeMillis();
            Path backupFolder = dataFolderPath.resolveSibling(backupFolderName);
            try {
                Files.move(dataFolderPath, backupFolder);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not move the plugin folder aside to \""
                        + backupFolderName + "\" before migrating the legacy configuration."
                        + " Chatty will not start, so your v2 files stay untouched.", e);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getLogger().log(Level.WARNING, "Found legacy \"config.yml\" file in plugin directory. \"Chatty\" folder was renamed to \"{0}\".", backupFolderName);
            legacyConfig = V2ConfigMigrator.readLegacyConfig(backupFolder.resolve("config.yml"));
            if (legacyConfig == null) {
                getLogger().warning("Could not read the legacy config.yml — starting with a default configuration.");
            }
        }

        initialize();

        if (legacyConfig != null) {
            try {
                // Generate fresh v3 defaults above, overwrite the migrated keys, then reload.
                new V2ConfigMigrator(getLogger()).migrate(legacyConfig, dataFolderPath);
                closeResources();
                initialize();
            } catch (Throwable t) {
                getLogger().log(Level.SEVERE, "Failed to migrate the legacy configuration."
                        + " Your v2 files are preserved in \"" + backupFolderName + "\": fix the problem"
                        + " reported below, move its config.yml back into the plugin folder and restart.", t);
                if (!startWithDefaultConfiguration(dataFolderPath)) {
                    getLogger().severe("Chatty is loaded but will NOT process chat:"
                            + " it could not start even with a default configuration."
                            + " Fix the errors above and restart the server.");
                    return;
                }
                getLogger().warning("Chatty started with a default configuration instead.");
            }
        }

        registerChattyCommand();
        registerChatCommands();

        releasePlayerLoginEvent();
    }

    private void releasePlayerLoginEvent() {
        try {
            EventUtil.unregisterListeners(PlayerLoginEvent.class, this);
        } catch (Throwable t) {
            getLogger().log(Level.FINE, "Cannot release the command framework's login listener", t);
        }
    }

    private boolean startWithDefaultConfiguration(Path dataFolderPath) {
        try {
            closeResources();
        } catch (Throwable ignored) {
        }
        try {
            if (Files.exists(dataFolderPath)) {
                Path quarantine = dataFolderPath.resolveSibling(
                        "Chatty_failed_migration_" + System.currentTimeMillis());
                Files.move(dataFolderPath, quarantine);
                getLogger().log(Level.WARNING,
                        "The half-migrated configuration was moved to \"{0}\".", quarantine.getFileName());
            }
            initialize();
            return true;
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Could not start with a default configuration", t);
            return false;
        }
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
                            "Use <yellow>/chatty reload</yellow> to reload configuration,<newline>" +
                            "or <yellow>/chatty broadcast <chat> <message></yellow> to send a message into a chat.");

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

        Command<CommandSender> broadcastCommand = chattyBuilder
                .literal("broadcast")
                .permission("chatty.command.broadcast")
                .argument(StringArgument.<CommandSender>builder("chat")
                        .single()
                        .withSuggestionsProvider((context, input) ->
                                new ArrayList<>(injector.getInstance(ChatRegistry.class).getChats().keySet()))
                        .build())
                .argument(StringArgument.greedy("message"))
                .handler(handler -> injector.getInstance(BroadcastCommandHandler.class).execute(handler))
                .build();

        syncCommandManager
                .command(infoCommand)
                .command(reloadCommand)
                .command(broadcastCommand);
    }

    private void initialize() throws Exception {
        BukkitAudiences audiences;
        if (isUseNativeAdventurePlatform()) {
            getLogger().log(Level.INFO, "Using native Adventure audience provider");
            audiences = new NativeBukkitAudienceProvider();
        } else {
            getLogger().log(Level.INFO, "Using bundled Adventure audience provider");
            audiences = BukkitAudiences.create(this);
        }

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

        registerChatListener(priority);

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

        MuteCommandHandler muteCommandHandler = injector.getInstance(MuteCommandHandler.class);
        registerProxyingHandler("mute", muteCommandHandler);

        if (this.asyncCommandManager == null) {
            initAsyncCommandManager();
            if (pmConfig.isEnable()) {
                registerPmCommands(commandSuggestionsProvider);
            }
            registerIgnoreCommand(commandSuggestionsProvider);
            registerMiscCommands();
            registerMuteCommands();
        }

        ChattyApiImpl.updateInstance(new ChattyApiImpl(injector.getInstance(ChatRegistry.class).getChats()));

        MetricsSender metricsSender = new MetricsSender();
        injector.injectMembers(metricsSender);
        metricsSender.run();
    }

    private void registerChatListener(EventPriority priority) {
        PluginManager pluginManager = this.getServer().getPluginManager();

        if (ModernEventExecutor.isSupported()) {
            try {
                Class<? extends Event> eventClass = ModernEventExecutor.getEventClass();
                ModernEventExecutor chatListener = injector.getInstance(ModernEventExecutor.class);
                chatListener.prepare();

                pluginManager.registerEvent(eventClass, chatListener, priority,
                        chatListener.earlyExecutor(), this, true);
                pluginManager.registerEvent(eventClass, chatListener, EventPriority.MONITOR,
                        chatListener.lateExecutor(), this, false);
                return;
            } catch (Throwable t) {
                getLogger().log(Level.WARNING,
                        "Cannot listen to the modern chat event, falling back to AsyncPlayerChatEvent", t);
            }
        }

        LegacyEventExecutor chatListener = injector.getInstance(LegacyEventExecutor.class);
        pluginManager.registerEvents(chatListener, this);
        pluginManager.registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, chatListener, this, true);
    }

    private void unregisterChatListener() {
        if (ModernEventExecutor.isSupported()) {
            try {
                EventUtil.unregisterListeners(ModernEventExecutor.getEventClass(), this);
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Cannot unregister the modern chat listener", t);
            }
        }
        EventUtil.unregisterListeners(AsyncPlayerChatEvent.class, this);
    }

    private void closeResources() throws IOException {
        MetricsSender.shutdownActive();
        if (injector != null) {
            injector.getInstance(ChatLogWriter.class).close();
        }
        if (!isUseNativeAdventurePlatform()) {
            BukkitAudiences.create(this).close();
        }
        if (injector != null) {
            injector.getInstance(PlayerDataRepository.class).close();
            injector.getInstance(ProxyService.class).close();
        }
        EventUtil.unregisterListeners(PlayerJoinEvent.class, this);
        EventUtil.unregisterListeners(PlayerQuitEvent.class, this);
        EventUtil.unregisterListeners(PlayerDeathEvent.class, this);
        unregisterChatListener();
        if (notificationTicker != null) {
            notificationTicker.cancelTicking();
        }
    }

    private void initAsyncCommandManager() throws Exception {
        this.asyncCommandManager = new BukkitCommandManager<>(this,
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder()
                        .withAsynchronousParsing()
                        .build(),
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

    private void registerMuteCommands() {
        ProxyingCommandHandler<CommandSender> handler = proxyingCommandHandlerMap.get("mute");

        asyncCommandManager.command(asyncCommandManager
                .commandBuilder("mute")
                .permission("chatty.command.mute")
                .argument(StringArgument.<CommandSender>builder("player").single()
                        .withSuggestionsProvider((context, input) -> Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName).collect(Collectors.toList()))
                        .build())
                .argument(StringArgument.<CommandSender>builder("options").greedy().asOptional().build())
                .handler(handler)
                .build());

        asyncCommandManager.command(asyncCommandManager
                .commandBuilder("unmute")
                .permission("chatty.command.mute")
                .argument(StringArgument.<CommandSender>builder("player").single()
                        .withSuggestionsProvider((context, input) -> Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName).collect(Collectors.toList()))
                        .build())
                .handler(context -> {
                    context.set("unmute", Boolean.TRUE);
                    handler.execute(context);
                })
                .build());
    }

    private void registerChatCommands() {
        ChatCommandHandler handler = injector.getInstance(ChatCommandHandler.class);
        for (Chat chat : injector.getInstance(ChatRegistry.class).getChats().values()) {
            ChatCommand chatCommand = chat.getCommand();
            if (chatCommand == null) {
                continue;
            }
            String[] aliases = chatCommand.getAliases().toArray(new String[0]);
            syncCommandManager.command(syncCommandManager
                    .commandBuilder(chatCommand.getName(), aliases)
                    .senderType(Player.class)
                    .argument(StringArgument.<CommandSender>builder("message").greedy().asOptional().build())
                    .handler(context -> {
                        context.set("chat-id", chat.getId());
                        handler.execute(context);
                    })
                    .build());
            getLogger().log(Level.INFO, "Registered chat command /{0} for chat \"{1}\"",
                    new Object[]{chatCommand.getName(), chat.getId()});
        }
    }

    private void registerMiscCommands() {
        var clearChatCommand = asyncCommandManager
                .commandBuilder("clearchat")
                .permission("chatty.command.clearchat")
                .argument(StringArgument.<CommandSender>builder("all")
                        .asOptional()
                        .build())
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
        PmConfig pmConfig = injector.getInstance(PmConfig.class);

        Command<CommandSender> msgCommand = asyncCommandManager
                .commandBuilder(pmConfig.getCommand(), pmConfig.getAliases().toArray(new String[0]))
                .permission(OrPermission.of(List.of(
                        Permission.of("chatty.pm"), Permission.of("chatty.command.msg"))))
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .argument(StringArgument.greedy("message"))
                .handler(proxyingCommandHandlerMap.get("msg"))
                .build();

        Command<CommandSender> replyCommand = asyncCommandManager
                .commandBuilder(pmConfig.getReplyCommand(), pmConfig.getReplyAliases().toArray(new String[0]))
                .permission(OrPermission.of(List.of(
                        Permission.of("chatty.pm"), Permission.of("chatty.command.reply"))))
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
        syncCommandManager = null;
        asyncCommandManager = null;
        proxyingCommandHandlerMap.clear();
        commandSuggestionsProvider = null;
        try {
            closeResources();
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Cannot gracefully shutdown Chatty", t);
        }
    }

    private void unregisterAllCommands(BukkitCommandManager<CommandSender> commandManager) {
        if (commandManager == null) {
            return;
        }
        for (Node<CommandArgument<CommandSender, ?>> node
                : new ArrayList<>(commandManager.commandTree().getRootNodes())) {
            CommandArgument<CommandSender, ?> argument = node.getValue();
            if (argument == null) {
                continue;
            }
            try {
                commandManager.deleteRootCommand(argument.getName());
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Could not unregister command /" + argument.getName(), t);
            }
        }
    }

    private static boolean isUseNativeAdventurePlatform() {
        return PaperUtil.isPaper() && PaperUtil.isSupportAdventure();
    }

}

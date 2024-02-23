package ru.brikster.chatty;

import cloud.commandframework.Command;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager.ManagerSettings;
import cloud.commandframework.CommandTree.Node;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.brikster.chatty.api.ChattyApiImpl;
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.command.CommandSuggestionsProvider;
import ru.brikster.chatty.command.ProxyingCommandHandler;
import ru.brikster.chatty.command.ProxyingCommandSuggestionsProvider;
import ru.brikster.chatty.command.handler.ClearChatCommandHandler;
import ru.brikster.chatty.config.file.ChatsConfig;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.guice.ConfigsLoader;
import ru.brikster.chatty.guice.GeneralGuiceModule;
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
import ru.brikster.chatty.spy.SpyCommandHandler;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.ListenerUtil;

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
        ChattyInitEvent initEvent = new ChattyInitEvent(BukkitAudiences.create(this));
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

        LegacyEventExecutor chatListener = injector.getInstance(LegacyEventExecutor.class);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, chatListener, this, true);

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

        SpyCommandHandler spyCommandHandler = injector.getInstance(SpyCommandHandler.class);
        registerProxyingHandler("spy", spyCommandHandler);

        ClearChatCommandHandler clearChatCommandHandler = injector.getInstance(ClearChatCommandHandler.class);
        registerProxyingHandler("clearchat", clearChatCommandHandler);

        if (this.asyncCommandManager == null) {
            initAsyncCommandManager();
            registerPmCommands(commandSuggestionsProvider);
            registerIgnoreCommand(commandSuggestionsProvider);
            registerClearChatCommand();
            registerSpyCommand();
        }

        ChattyApiImpl.updateInstance(new ChattyApiImpl(injector.getInstance(ChatRegistry.class).getChats()));
    }

    private void closeResources() throws IOException {
        BukkitAudiences.create(this).close();
        injector.getInstance(PlayerDataRepository.class).close();
        injector.getInstance(ProxyService.class).close();
        ListenerUtil.unregister(PlayerJoinEvent.class, this);
        ListenerUtil.unregister(PlayerQuitEvent.class, this);
        ListenerUtil.unregister(PlayerDeathEvent.class, this);
        ListenerUtil.unregister(AsyncPlayerChatEvent.class, this);
        notificationTicker.cancelTicking();
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

    private void registerSpyCommand() {
        var command = asyncCommandManager
                .commandBuilder("spy")
                .permission("chatty.command.spy")
                .handler(proxyingCommandHandlerMap.get("spy"))
                .build();
        asyncCommandManager.command(command);
    }

    private void registerClearChatCommand() {
        var command = asyncCommandManager
                .commandBuilder("clearchat")
                .permission("chatty.command.clearchat")
                .handler(proxyingCommandHandlerMap.get("clearchat"))
                .build();
        asyncCommandManager.command(command);
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
        this.getServer().getScheduler().cancelTasks(this);
        unregisterAllCommands(syncCommandManager);
        unregisterAllCommands(asyncCommandManager);
        try {
            closeResources();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot gracefully shutdown Chatty", e);
        }
    }

    private static void unregisterAllCommands(BukkitCommandManager<CommandSender> commandManager) {
        for (Node<CommandArgument<CommandSender, ?>> node : commandManager.commandTree().getRootNodes()) {
            //noinspection DataFlowIssue
            commandManager.deleteRootCommand(node.getValue().getName());
        }
    }

}

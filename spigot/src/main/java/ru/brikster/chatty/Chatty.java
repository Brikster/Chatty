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
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.command.ProxyCommandHandler;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.PmConfig;
import ru.brikster.chatty.config.type.SettingsConfig;
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
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.ListenerUtil;

import java.util.function.Function;
import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

    private Injector injector;

    private NotificationTicker notificationTicker;
    private BukkitCommandManager<CommandSender> syncCommandManager;
    private BukkitCommandManager<CommandSender> asyncCommandManager;

    private ProxyCommandHandler<MsgCommandHandler, CommandSender> msgProxiedCommandHandler;
    private ProxyCommandHandler<ReplyCommandHandler, CommandSender> replyProxiedCommandHandler;
    private ProxyCommandHandler<AddIgnoreCommandHandler, CommandSender> addIgnoreProxiedCommandHandler;
    private ProxyCommandHandler<RemoveIgnoreCommandHandler, CommandSender> removeIgnoreProxiedCommandHandler;
    private ProxyCommandHandler<IgnoreListCommandHandler, CommandSender> ignoreListProxiedCommandHandler;

    @SneakyThrows
    @Override
    public void onEnable() {
        ChattyInitEvent initEvent = new ChattyInitEvent(BukkitAudiences.create(this));
        getServer().getPluginManager().callEvent(initEvent);
        initialize(initEvent);
        registerChattyCommand(initEvent);
    }

    private void registerChattyCommand(ChattyInitEvent initEvent) throws Exception {
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
                    //noinspection resource
                    BukkitAudiences.create(this).sender(handler.getSender()).sendMessage(component);
                }).build();

        Command<CommandSender> reloadCommand = chattyBuilder
                .literal("reload")
                .permission("chatty.command.reload")
                .handler(handler -> {
                    try {
                        injector.getInstance(PlayerDataRepository.class).close();
                        ListenerUtil.unregister(PlayerJoinEvent.class, this);
                        ListenerUtil.unregister(PlayerQuitEvent.class, this);
                        ListenerUtil.unregister(PlayerDeathEvent.class, this);
                        ListenerUtil.unregister(AsyncPlayerChatEvent.class, this);
                        notificationTicker.cancelTicking();
                        initialize(initEvent);
                        handler.getSender().sendMessage("§aPlugin successfully reloaded!");
                    } catch (Throwable t) {
                        handler.getSender().sendMessage("§cError while reloading plugin: " + t.getClass().getSimpleName() + ". See console for more details.");
                    }
                })
                .build();

        syncCommandManager
                .command(infoCommand)
                .command(reloadCommand);
    }

    private void initialize(ChattyInitEvent initEvent) throws Exception {
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

        AddIgnoreCommandHandler addIgnoreCommandHandler = injector.getInstance(AddIgnoreCommandHandler.class);
        RemoveIgnoreCommandHandler removeIgnoreCommandHandler = injector.getInstance(RemoveIgnoreCommandHandler.class);
        IgnoreListCommandHandler ignoreListCommandHandler = injector.getInstance(IgnoreListCommandHandler.class);

        if (this.asyncCommandManager == null) {
            addIgnoreProxiedCommandHandler = new ProxyCommandHandler<>(addIgnoreCommandHandler);
            removeIgnoreProxiedCommandHandler = new ProxyCommandHandler<>(removeIgnoreCommandHandler);
            ignoreListProxiedCommandHandler = new ProxyCommandHandler<>(ignoreListCommandHandler);

            initAsyncCommandManager();

            if (pmConfig.isEnable()) {
                MsgCommandHandler msgCommandHandler = injector.getInstance(MsgCommandHandler.class);
                ReplyCommandHandler replyCommandHandler = injector.getInstance(ReplyCommandHandler.class);
                msgProxiedCommandHandler = new ProxyCommandHandler<>(msgCommandHandler);
                replyProxiedCommandHandler = new ProxyCommandHandler<>(replyCommandHandler);
                registerPmCommands(pmSuggestionsProvider);
            }

            registerIgnoreCommand(pmSuggestionsProvider);
        } else {
            if (pmConfig.isEnable()) {
                MsgCommandHandler msgCommandHandler = injector.getInstance(MsgCommandHandler.class);
                ReplyCommandHandler replyCommandHandler = injector.getInstance(ReplyCommandHandler.class);
                msgProxiedCommandHandler.setExecutionHandler(msgCommandHandler);
                replyProxiedCommandHandler.setExecutionHandler(replyCommandHandler);
                registerPmCommands(pmSuggestionsProvider);
            }

            addIgnoreProxiedCommandHandler.setExecutionHandler(addIgnoreCommandHandler);
            removeIgnoreProxiedCommandHandler.setExecutionHandler(removeIgnoreCommandHandler);
            ignoreListProxiedCommandHandler.setExecutionHandler(ignoreListCommandHandler);
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

        //noinspection resource
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
                .apply(asyncCommandManager, BukkitAudiences.create(this)::sender);

        asyncCommandManager.setSetting(ManagerSettings.ALLOW_UNSAFE_REGISTRATION, true);
    }

    private void registerIgnoreCommand(PrivateMessageSuggestionsProvider pmSuggestionsProvider) {
        Builder<CommandSender> ignoreCommandBuilder = asyncCommandManager
                 .commandBuilder("ignore")
                 .permission("chatty.command.ignore");

        Command<CommandSender> ignoreAddCommand = ignoreCommandBuilder
                .literal("add")
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .handler(addIgnoreProxiedCommandHandler)
                .build();

        Command<CommandSender> ignoreRemoveCommand = ignoreCommandBuilder
                .literal("remove", "rem", "rm", "delete")
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .handler(removeIgnoreProxiedCommandHandler)
                .build();

        Command<CommandSender> ignoreListCommand = ignoreCommandBuilder
                .handler(ignoreListProxiedCommandHandler)
                .build();

        asyncCommandManager
                .command(ignoreAddCommand)
                .command(ignoreRemoveCommand)
                .command(ignoreListCommand);
    }

    private void registerPmCommands(PrivateMessageSuggestionsProvider pmSuggestionsProvider) {
        Command<CommandSender> msgCommand = asyncCommandManager.commandBuilder("msg", "message", "m", "w", "pm", "dm")
                .permission("chatty.pm")
                .argument(StringArgument.<CommandSender>builder("target")
                        .single()
                        .withSuggestionsProvider(pmSuggestionsProvider)
                        .build())
                .argument(StringArgument.greedy("message"))
                .handler(msgProxiedCommandHandler)
                .build();

        Command<CommandSender> replyCommand = asyncCommandManager.commandBuilder("reply", "r")
                .permission("chatty.pm")
                .argument(StringArgument.greedy("message"))
                .handler(replyProxiedCommandHandler)
                .build();

        asyncCommandManager
                .command(msgCommand)
                .command(replyCommand);
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        unregisterAllCommands(syncCommandManager);
        unregisterAllCommands(asyncCommandManager);
    }

    private static void unregisterAllCommands(BukkitCommandManager<CommandSender> commandManager) {
        for (Node<CommandArgument<CommandSender, ?>> node : commandManager.commandTree().getRootNodes()) {
            //noinspection DataFlowIssue
            commandManager.deleteRootCommand(node.getValue().getName());
        }
    }

}

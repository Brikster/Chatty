package ru.brikster.chatty;

import cloud.commandframework.Command;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
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
import ru.brikster.chatty.config.type.SettingsConfig;
import ru.brikster.chatty.guice.ConfigsLoader;
import ru.brikster.chatty.guice.GeneralGuiceModule;
import ru.brikster.chatty.misc.VanillaListener;
import ru.brikster.chatty.notification.NotificationTicker;
import ru.brikster.chatty.papi.PapiExpansionInstaller;
import ru.brikster.chatty.util.ListenerUtil;

import java.util.function.Function;
import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

    private NotificationTicker notificationTicker;

    @SneakyThrows
    @Override
    public void onEnable() {
        ChattyInitEvent initEvent = new ChattyInitEvent(BukkitAudiences.create(this));
        getServer().getPluginManager().callEvent(initEvent);
        initialize(initEvent);
        registerChattyCommand(initEvent);
    }

    private void registerChattyCommand(ChattyInitEvent initEvent) throws Exception {
        BukkitCommandManager<CommandSender> syncCommandManager = new BukkitCommandManager<>(this,
                CommandExecutionCoordinator.simpleCoordinator(),
//                AsynchronousCommandExecutionCoordinator.<CommandSender>builder()
//                        .withAsynchronousParsing()
//                        .build(),
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
                .permission("chatty.reload")
                .handler(handler -> {
                    try {
                        ListenerUtil.unregister(PlayerJoinEvent.class, this);
                        ListenerUtil.unregister(PlayerQuitEvent.class, this);
                        ListenerUtil.unregister(PlayerDeathEvent.class, this);
                        ListenerUtil.unregister(AsyncPlayerChatEvent.class, this);
                        notificationTicker.cancelTicking();
                        initialize(initEvent);
                        handler.getSender().sendMessage("§aPlugin successfully reloaded!");
                    } catch (Throwable t) {
                        handler.getSender().sendMessage("§cError while reloading plugin: " + t.getCause().getMessage() + ". See console for more details.");
                    }
                })
                .build();

        syncCommandManager
                .command(infoCommand)
                .command(reloadCommand);
    }

    private void initialize(ChattyInitEvent initEvent) {
        Injector injector = Guice.createInjector(new GeneralGuiceModule(
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
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
    }

}

package ru.mrbrikster.chatty;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;
import ru.mrbrikster.chatty.api.ChattyApi;
import ru.mrbrikster.chatty.api.ChattyApiImplementation;
import ru.mrbrikster.chatty.bungee.BungeeCordListener;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatListener;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.commands.CommandManager;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.notifications.NotificationManager;
import ru.mrbrikster.chatty.util.Debugger;
import ru.mrbrikster.chatty.util.Messages;

import java.io.File;
import java.util.stream.Collectors;

public final class Chatty extends BukkitBasePlugin {

    private static Chatty instance;
    private static ChattyApi api;
    private CommandManager commandManager;
    private Messages messages;
    private Debugger debugger;
    private Configuration configuration;
    private ChatManager chatManager;
    private JsonStorage jsonStorage;
    private DependencyManager dependencyManager;

    public static Chatty instance() {
        return Chatty.instance;
    }

    /**
     * Returns API object for interacting with Chatty
     * @return API object
     */
    public ChattyApi api() {
        return Chatty.api;
    }

    public ChatManager chat() {
        return this.chatManager;
    }

    public Messages messages() {
        return this.messages;
    }

    public Debugger debugger() {
        return this.debugger;
    }

    public JsonStorage storage() {
        return this.jsonStorage;
    }

    public DependencyManager dependencies() {
        return this.dependencyManager;
    }

    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        configuration = getConfiguration();

        if (!configuration.getNode("config-version").getAsString("0.0").equals("2.0")) {
            File file = new File(getDataFolder(), "config.yml");
            file.renameTo(new File(getDataFolder(), "config.yml.old"));

            configuration = getConfiguration("config.yml");
        }

        ModerationManager moderationManager = new ModerationManager(this, configuration);
        this.jsonStorage = new JsonStorage(configuration, this);
        this.chatManager = new ChatManager(configuration, jsonStorage);
        this.dependencyManager = new DependencyManager(configuration, jsonStorage, this);

        this.messages = new Messages(this, configuration);
        this.debugger = new Debugger(this, configuration);

        configuration.onReload(config -> this.messages = new Messages(this, config));

        this.commandManager = new CommandManager(configuration, chatManager, dependencyManager, jsonStorage, moderationManager);
        new NotificationManager(configuration);

        EventPriority eventPriority;
        try {
            String priorityName = configuration.getNode("general.priority").getAsString("normal").toUpperCase();
            eventPriority = EventPriority.valueOf(priorityName);

            if (eventPriority == EventPriority.MONITOR) {
                eventPriority = EventPriority.NORMAL;
            }
        } catch (IllegalArgumentException e) {
            eventPriority = EventPriority.NORMAL;
        }

        ChatListener chatListener = new ChatListener(configuration, chatManager, dependencyManager, moderationManager, jsonStorage);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, eventPriority, chatListener, Chatty.instance, true);

        if (configuration.getNode("general.metrics").getAsBoolean(true)) {
            Metrics metrics = new Metrics(this, 3466);
            metrics.addCustomChart(new Metrics.SimplePie("language",
                    () -> configuration.getNode("general.locale").getAsString("en")));

            metrics.addCustomChart(new Metrics.SimplePie("json",
                    () -> String.valueOf(configuration.getNode("json.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("private_messaging",
                    () -> String.valueOf(configuration.getNode("pm.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("spy",
                    () -> String.valueOf(configuration.getNode("spy.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("chat_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.chat.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("actionbar_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.actionbar.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("advancements_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.advancements.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("caps_moderation_method",
                    () -> String.valueOf(configuration.getNode("moderation.caps.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("adv_moderation_method",
                    () -> String.valueOf(configuration.getNode("moderation.advertisement.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("swear_moderation_method",
                    () -> String.valueOf(configuration.getNode("moderation.swear.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("miscellaneous_auto_nte",
                    () -> String.valueOf(configuration.getNode("miscellaneous.commands.prefix.auto-nte").getAsBoolean(false)
                            || configuration.getNode("miscellaneous.commands.suffix.auto-nte").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("miscellaneous_join_msg",
                    () -> String.valueOf(configuration.getNode("miscellaneous.vanilla.join.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("miscellaneous_quit_msg",
                    () -> String.valueOf(configuration.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("miscellaneous_death_msg",
                    () -> String.valueOf(configuration.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("uuid",
                    () -> String.valueOf(configuration.getNode("general.uuid").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("bungeecord",
                    () -> String.valueOf(configuration.getNode("general.bungeecord").getAsBoolean(false))));

            metrics.addCustomChart(new Metrics.SimplePie("debug",
                    () -> String.valueOf(configuration.getNode("general.debug").getAsBoolean(false))));
        }

        if (configuration.getNode("general.bungeecord").getAsBoolean(false)) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener(chatManager));
        }

        Chatty.api = new ChattyApiImplementation(chatManager.getChats().stream().filter(Chat::isEnable).collect(Collectors.toSet()));
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.commandManager.unregisterAll();
        this.chatManager.getChats().forEach(chat -> {
            if (chat.getBukkitCommand() != null) {
                chat.getBukkitCommand().unregister(Chatty.instance());
            }
        });
    }

}

package ru.mrbrikster.chatty;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;
import ru.mrbrikster.chatty.bungee.BungeeCordListener;
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

public final class Chatty extends BukkitBasePlugin {

    private static Chatty instance;
    private CommandManager commandManager;
    private Messages messages;
    private Debugger debugger;
    private Configuration configuration;

    public static Chatty instance() {
        return Chatty.instance;
    }

    public Messages messages() {
        return this.messages;
    }

    public Debugger debugger() {
        return this.debugger;
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

        ChatManager chatManager = new ChatManager(configuration);
        ModerationManager moderationManager = new ModerationManager(this, configuration);
        JsonStorage jsonStorage = new JsonStorage(configuration, this);
        DependencyManager dependencyManager = new DependencyManager(configuration, jsonStorage, this);

        this.messages = new Messages(this, configuration);
        this.debugger = new Debugger(this, configuration);

        configuration.onReload(config -> this.messages = new Messages(this, config));

        this.commandManager = new CommandManager(configuration, dependencyManager, jsonStorage, moderationManager);
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
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SimplePie("language",
                    () -> configuration.getNode("general.locale").getAsString("en")));
            metrics.addCustomChart(new Metrics.SimplePie("json",
                    () -> String.valueOf(configuration.getNode("json.enable").getAsBoolean(false))));
            metrics.addCustomChart(new Metrics.SimplePie("chat_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.chat.enable").getAsBoolean(false))));
            metrics.addCustomChart(new Metrics.SimplePie("actionbar_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.actionbar.enable").getAsBoolean(false))));
            metrics.addCustomChart(new Metrics.SimplePie("advancements_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.advancements.enable").getAsBoolean(false))));
            metrics.addCustomChart(new Metrics.SimplePie("debug",
                    () -> String.valueOf(configuration.getNode("general.debug").getAsBoolean(false))));
        }

        if (configuration.getNode("general.bungeecord").getAsBoolean(false)) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener(chatManager));
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.commandManager.unregisterAll();
    }

}

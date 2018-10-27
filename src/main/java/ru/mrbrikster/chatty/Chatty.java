package ru.mrbrikster.chatty;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.chat.ChatListener;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.commands.CommandManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.notifications.NotificationManager;

public final class Chatty extends JavaPlugin {

    private static Chatty instance;
    private CommandManager commandManager;

    public static Chatty instance() {
        return Chatty.instance;
    }

    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        Configuration configuration = new Configuration(this);
        DependencyManager dependencyManager = new DependencyManager(this);
        ChatManager chatManager = new ChatManager(configuration);
        ModerationManager moderationManager = new ModerationManager(this, configuration);
        TemporaryStorage temporaryStorage = new TemporaryStorage();
        PermanentStorage permanentStorage = new PermanentStorage(this);

        this.commandManager = new CommandManager(configuration, temporaryStorage, permanentStorage);
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

        ChatListener chatListener = new ChatListener(
                configuration,
                chatManager,
                temporaryStorage,
                dependencyManager,
                moderationManager,
                permanentStorage);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(
                AsyncPlayerChatEvent.class,
                chatListener,
                eventPriority,
                chatListener,
                Chatty.instance,
                true);

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
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.commandManager.unregisterAll();
    }

}

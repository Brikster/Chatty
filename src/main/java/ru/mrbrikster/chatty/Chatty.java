package ru.mrbrikster.chatty;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.commands.CommandManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.listeners.ChatListener;
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

        EventPriority priority;
        try {
            String priorityName = configuration.getNode("general.priority").getAsString("normal").toUpperCase();
            priority = EventPriority.valueOf(priorityName);
        } catch (IllegalArgumentException e) {
            priority = EventPriority.NORMAL;
        }

        if (priority == EventPriority.MONITOR) {
            priority = EventPriority.NORMAL;
        }

        ChatListener chatListener = new ChatListener(
                configuration, chatManager, temporaryStorage, dependencyManager, moderationManager, permanentStorage
        );

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(
                AsyncPlayerChatEvent.class,
                chatListener,
                priority,
                chatListener,
                this,
                true
        );
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.commandManager.unregisterAll();
    }

}

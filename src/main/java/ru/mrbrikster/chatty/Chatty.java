package ru.mrbrikster.chatty;

import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.chat.ChatManager;
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
        ModerationManager moderationManager = new ModerationManager(configuration);

        this.commandManager = new CommandManager(configuration, chatManager);
        new NotificationManager(configuration);

        ChatListener chatListener;
        try {
            chatListener = (ChatListener) Class.forName(String.format("ru.mrbrikster.chatty.listeners.%s",
                    configuration.getNode("general.priority").getAsString("normal").toUpperCase()))
                    .getConstructor(Configuration.class, ChatManager.class, DependencyManager.class, ModerationManager.class)
                    .newInstance(configuration, chatManager, dependencyManager, moderationManager);
        } catch (Exception ex) {
            chatListener = new ru.mrbrikster.chatty.listeners.NORMAL(configuration, chatManager, dependencyManager, moderationManager);
        }

        this.getServer().getPluginManager().registerEvents(chatListener, this);
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.commandManager.unregisterAll();
    }

}

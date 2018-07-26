package ru.mrbrikster.chatty;

import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.commands.CommandManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyPool;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.listeners.ChatListener;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.notifications.NotificationManager;

import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

    private static Chatty instance;
    private DependencyPool dependencyPool;

    public static Chatty instance() {
        return Chatty.instance;
    }

    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        Configuration configuration = new Configuration(this);

        this.dependencyPool = new DependencyPool();

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            dependencyPool.putDependency(new VaultHook());
            getLogger().log(Level.INFO, "Vault has successful hooked.");
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            dependencyPool.putDependency(new PlaceholderAPIHook());
            getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
        }

        ChatManager chatManager = new ChatManager(configuration);
        ModerationManager moderationManager = new ModerationManager(configuration);

        new CommandManager(configuration, chatManager);
        new NotificationManager(configuration);

        ChatListener chatListener;
        try {
            chatListener = (ChatListener) Class.forName(String.format("ru.mrbrikster.chatty.listeners.%s",
                    configuration.getNode("general.priority").getAsString("normal").toUpperCase()))
                    .getConstructor(Configuration.class, ChatManager.class, DependencyPool.class, ModerationManager.class)
                    .newInstance(configuration, chatManager, dependencyPool, moderationManager);
        } catch (Exception ex) {
            chatListener = new ru.mrbrikster.chatty.listeners.NORMAL(configuration, chatManager, dependencyPool, moderationManager);
        }

        this.getServer().getPluginManager().registerEvents(chatListener, this);

    }

    @Override
    public void onDisable() {
        this.dependencyPool.removeAll();
    }

}

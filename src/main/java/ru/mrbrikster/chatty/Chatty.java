package ru.mrbrikster.chatty;

import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.commands.CommandManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyPool;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.listeners.ChatListener;

import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

    private static Chatty instance;
    private DependencyPool dependencyPool;
    private ChatManager chatManager;
    private CommandManager commandManager;
    private Configuration configuration;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        this.dependencyPool = new DependencyPool();

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            dependencyPool.putDependency(new VaultHook(this));
            getLogger().log(Level.INFO, "Vault has successful hooked.");
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")){
            dependencyPool.putDependency(new PlaceholderAPIHook());
            getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
        }

        this.configuration = new Configuration(this);
        this.commandManager = new CommandManager(this);
        this.chatManager = new ChatManager(configuration);

        try {
            this.chatListener = (ChatListener) Class.forName(String.format("ru.mrbrikster.chatty.listeners.%s",
                    configuration.getNode("general.priority").getAsString("normal").toUpperCase()))
                    .getConstructor(Configuration.class, ChatManager.class, DependencyPool.class)
                    .newInstance(configuration, chatManager, dependencyPool);
        } catch (Exception ex) {
            this.chatListener = new ru.mrbrikster.chatty.listeners.NORMAL(configuration, chatManager, dependencyPool);
        }

        this.getServer().getPluginManager().registerEvents(chatListener, this);
    }

    @Override
    public void onDisable() {
        this.dependencyPool.removeAll();
    }

    public void init() {
        /*
        if (this.configuration != null)
            configuration.getAlertsLists().forEach(AlertsManager.AlertList::cancel);

        this.configuration = new Config(this);

        // Enable announcements manager
        if (configuration.isAnnouncementsEnabled())
            this.announcementsManager = new AnnouncementsManager(this);

        if (configuration.isAlertsEnabled())
            this.alertsManager = new AlertsManager(this);
            */
    }

    public static Chatty instance() {
        return Chatty.instance;
    }

}

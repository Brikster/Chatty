package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.managers.*;

public final class Main extends JavaPlugin {

    @Getter private EventManager eventManager;
    @Getter private Config configuration;
    @Getter private VaultManager vaultManager;
    @Getter private ViewManager viewManager;
    @Getter private EconomyManager economyManager;
    @Getter private CommandManager commandManager;
    @Getter private LogManager logManager;
    @Getter private AnnouncementsManager announcementsManager;
    @Getter private AlertsManager alertsManager;

    @Override
    public void onEnable() {
        this.init();

        // Init command manager
        this.commandManager = new CommandManager(this);

        // Init EventManager with configured priority
        try {
            this.eventManager = (EventManager) Class.forName("ru.mrbrikster.chatty.listeners." + configuration.getPriority().name()).getConstructor(Main.class).newInstance(this);
        } catch (Exception ex) {
            this.eventManager = new ru.mrbrikster.chatty.listeners.NORMAL(this);
        }

        // Init log-manager
        this.logManager = new LogManager(this);
    }

    public void init() {
        // Cancel all alerts tasks
        if (this.configuration != null)
            configuration.getAlertsLists().forEach(AlertsManager.AlertList::cancel);

        // Init config
        this.configuration = new Config(this);

        // Load class with Vault-imports
        if (Bukkit.getPluginManager().getPlugin("Vault") != null)
            this.vaultManager = new VaultManager(this);

        // Init Vault-depended managers
        this.viewManager = new ViewManager(this);
        this.economyManager = new EconomyManager(this);

        // Enable announcements manager
        if (configuration.isAnnouncementsEnabled())
            this.announcementsManager = new AnnouncementsManager(this);

        // Enable alerts manager
        if (configuration.isAlertsEnabled())
            this.alertsManager = new AlertsManager(this);
    }

}

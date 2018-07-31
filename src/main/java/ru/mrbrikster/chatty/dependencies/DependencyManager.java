package ru.mrbrikster.chatty.dependencies;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class DependencyManager {

    @Getter private VaultHook vault;
    @Getter private PlaceholderAPIHook placeholderApi;

    public DependencyManager(JavaPlugin javaPlugin) {
        if (javaPlugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.vault = new VaultHook();
            javaPlugin.getLogger().log(Level.INFO, "Vault has successful hooked.");
        }

        if (javaPlugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderApi = new PlaceholderAPIHook();
            javaPlugin.getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
        }
    }

}

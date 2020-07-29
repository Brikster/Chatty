package ru.mrbrikster.chatty.dependencies;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.util.logging.Level;

public class DependencyManager {

    @Getter private VaultHook vault;
    @Getter private PlaceholderAPIHook placeholderApi;
    @Getter private NametagEditHook nametagEdit;

    public DependencyManager(Configuration configuration,
                             JsonStorage jsonStorage, ChatManager chatManager, JavaPlugin javaPlugin) {
        if (javaPlugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.vault = new VaultHook();
            javaPlugin.getLogger().log(Level.INFO, "Vault has successful hooked.");
        }

        if (javaPlugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderApi = new PlaceholderAPIHook(chatManager);
            placeholderApi.register();
            javaPlugin.getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
        }

        if (javaPlugin.getServer().getPluginManager().isPluginEnabled("NametagEdit")) {
            this.nametagEdit = new NametagEditHook(configuration, jsonStorage);
            javaPlugin.getLogger().log(Level.INFO, "NametagEdit has successful hooked.");
        }
    }

}

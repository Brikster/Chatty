package ru.mrbrikster.chatty.dependencies;

import lombok.Getter;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.util.logging.Level;

public class DependencyManager {

    @Getter private VaultHook vault;
    @Getter private PlaceholderAPIHook placeholderApi;
    @Getter private NametagEditHook nametagEdit;

    public DependencyManager(Chatty chatty) {
        Configuration configuration = chatty.getExact(Configuration.class);
        JsonStorage jsonStorage = chatty.getExact(JsonStorage.class);

        if (chatty.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.vault = new VaultHook();
            chatty.getLogger().log(Level.INFO, "Vault has successful hooked.");
        }

        if (chatty.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderApi = new PlaceholderAPIHook();
            chatty.getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
        }

        if (chatty.getServer().getPluginManager().isPluginEnabled("NametagEdit")) {
            this.nametagEdit = new NametagEditHook(configuration, jsonStorage);
            chatty.getLogger().log(Level.INFO, "NametagEdit has successful hooked.");
        }
    }

}

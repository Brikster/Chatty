package ru.mrbrikster.chatty.dependencies;

import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.util.logging.Level;

@Getter
public class DependencyManager {

    @Getter private VaultHook vault;
    @Getter private PlaceholderAPIHook placeholderApi;
    @Getter private NametagEditHook nametagEdit;
    @Getter private EssentialsHook essentials;

    public DependencyManager(Chatty chatty) {
        Configuration configuration = chatty.getExact(Configuration.class);
        JsonStorage jsonStorage = chatty.getExact(JsonStorage.class);
        ChatManager chatManager = chatty.getExact(ChatManager.class);

        if (chatty.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.vault = new VaultHook();
            chatty.getLogger().log(Level.INFO, "Vault has successful hooked.");
        }

        if (chatty.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderApi = new PlaceholderAPIHook(chatManager);
            placeholderApi.register();
            chatty.getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
        }

        if (chatty.getServer().getPluginManager().isPluginEnabled("NametagEdit")) {
            this.nametagEdit = new NametagEditHook(configuration, jsonStorage);
            chatty.getLogger().log(Level.INFO, "NametagEdit has successful hooked.");
        }

        chatty.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onServerLoad(ServerLoadEvent event) {

                if (chatty.getServer().getPluginManager().isPluginEnabled("Essentials")) {
                    DependencyManager.this.essentials = new EssentialsHook(chatty);
                    chatty.getLogger().log(Level.INFO, "Essentials has successful hooked.");
                }
            }

        }, chatty);
    }

}

package ru.mrbrikster.chatty.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Main;

public class ViewManager {

    private final boolean enable;
    private VaultManager vaultManager;

    public ViewManager(Main main) {
        if (this.enable = (Bukkit.getPluginManager().getPlugin("Vault") != null)) {
            this.vaultManager = new VaultManager(main);
        }
    }

    String getPrefix(Player player) {
        return enable ? vaultManager.getChat()
                .getPlayerPrefix(player) : "";
    }

    String getSuffix(Player player) {
        return enable ? vaultManager.getChat()
                .getPlayerSuffix(player) : "";
    }

}

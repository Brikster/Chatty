package ru.mrbrikster.chatty.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Main;

public class ViewManager {

    private final boolean enable;
    private VaultManager vaultManager;

    public ViewManager(Main main) {
        this.enable = Bukkit.getPluginManager().getPlugin("Vault") != null;

        if (enable) {
            this.vaultManager = new VaultManager(main);
        }
    }


    String getPrefix(Player player) {
        if (enable) {
            return vaultManager.getChat()
                    .getPlayerPrefix(player);
        } else {
            return "";
        }
    }

    String getSuffix(Player player) {
        if (enable) {
            return vaultManager.getChat()
                    .getPlayerSuffix(player);
        } else {
            return "";
        }
    }

}

package ru.mrbrikster.chatty.managers;

import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Main;

public class ViewManager {

    private final boolean enable;
    private VaultManager vaultManager;

    public ViewManager(Main main) {
        if (this.enable = main.getVaultManager() != null) {
            this.vaultManager = main.getVaultManager();
        }
    }

    String getPrefix(Player player) {
        return isChatActive() ? vaultManager.getChat()
                .getPlayerPrefix(player) : "";
    }

    String getSuffix(Player player) {
        return isChatActive() ? vaultManager.getChat()
                .getPlayerSuffix(player) : "";
    }

    private boolean isChatActive() {
        return enable && vaultManager.getChat() != null;
    }

}

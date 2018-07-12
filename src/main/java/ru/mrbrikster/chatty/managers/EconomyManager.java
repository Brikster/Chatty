package ru.mrbrikster.chatty.managers;

import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Main;

public class EconomyManager {

    private final boolean enable;
    private VaultManager vaultManager;

    public EconomyManager(Main main) {
        if (this.enable = main.getVaultManager() != null) {
            this.vaultManager = main.getVaultManager();
        }
    }

    public boolean withdraw(Player player, int amount) {
        return isEconomyActive() && vaultManager.getEconomy().withdrawPlayer(player, amount).transactionSuccess();
    }

    private boolean isEconomyActive() {
        return enable && vaultManager.getEconomy() != null;
    }

}

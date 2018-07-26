package ru.mrbrikster.chatty.dependencies;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final Chat chat;
    private final Economy economy;

    public VaultHook() {
        RegisteredServiceProvider<Chat> chatRegisteredServiceProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
        RegisteredServiceProvider<Economy> economyRegisteredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (chatRegisteredServiceProvider != null)
            this.chat = chatRegisteredServiceProvider.getProvider();
        else this.chat = null;

        if (economyRegisteredServiceProvider != null)
            this.economy = economyRegisteredServiceProvider.getProvider();
        else this.economy = null;
    }

    public boolean withdrawMoney(Player player, int amount) {
        return economy != null && economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public String getPrefix(Player player) {
        if (chat != null) {
            return chat.getPlayerPrefix(player);
        }

        return null;
    }

    public String getSuffix(Player player) {
        if (chat != null) {
            return chat.getPlayerSuffix(player);
        }

        return null;
    }

}

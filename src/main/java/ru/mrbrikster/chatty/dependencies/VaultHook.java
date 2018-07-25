package ru.mrbrikster.chatty.dependencies;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.mrbrikster.chatty.Chatty;

public class VaultHook {

    private final Chat chat;
    private final Economy economy;

    public VaultHook(Chatty chatty) {
        RegisteredServiceProvider<Chat> chatRegisteredServiceProvider = chatty.getServer().getServicesManager().getRegistration(Chat.class);
        RegisteredServiceProvider<Economy> economyRegisteredServiceProvider = chatty.getServer().getServicesManager().getRegistration(Economy.class);

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

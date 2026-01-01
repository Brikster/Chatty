package ru.brikster.chatty.prefix;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.inject.Singleton;

@Singleton
public final class VaultPrefixProvider implements PrefixProvider {

    private final net.milkbowl.vault.chat.Chat vaultChatModule;

    public VaultPrefixProvider() {
        var registration = Bukkit.getServicesManager().getRegistration(Chat.class);
        this.vaultChatModule = registration != null ? registration.getProvider() : null;
    }

    @Override
    public String getPrefix(OfflinePlayer player) {
        if (vaultChatModule == null) {
            return null;
        }
        String worldName;
        if (player instanceof Player) {
            worldName = ((Player) player).getWorld().getName();
        } else if (!Bukkit.getWorlds().isEmpty()) {
            worldName = Bukkit.getWorlds().get(0).getName();
        } else {
            worldName = null;
        }
        return vaultChatModule.getPlayerPrefix(worldName, player);
    }

    @Override
    public String getSuffix(OfflinePlayer player) {
        if (vaultChatModule == null) {
            return null;
        }
        String worldName;
        if (player instanceof Player) {
            worldName = ((Player) player).getWorld().getName();
        } else if (!Bukkit.getWorlds().isEmpty()) {
            worldName = Bukkit.getWorlds().get(0).getName();
        } else {
            worldName = null;
        }
        return vaultChatModule.getPlayerSuffix(worldName, player);
    }

}

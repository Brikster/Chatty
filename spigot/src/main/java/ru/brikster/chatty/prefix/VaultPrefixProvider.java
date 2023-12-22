package ru.brikster.chatty.prefix;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class VaultPrefixProvider implements PrefixProvider {

    private final net.milkbowl.vault.chat.Chat vaultChatModule =
            Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(Chat.class)).getProvider();

    @Override
    public String getPrefix(OfflinePlayer player) {
        return vaultChatModule.getPlayerPrefix(player instanceof Player
                ? ((Player) player).getWorld().getName()
                : Bukkit.getWorlds().get(0).getName(), player);
    }

    @Override
    public String getSuffix(OfflinePlayer player) {
        return vaultChatModule.getPlayerSuffix(player instanceof Player
                ? ((Player) player).getWorld().getName()
                : Bukkit.getWorlds().get(0).getName(), player);
    }

}

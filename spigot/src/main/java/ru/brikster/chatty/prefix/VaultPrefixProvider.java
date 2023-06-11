package ru.brikster.chatty.prefix;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class VaultPrefixProvider implements PrefixProvider {

    private final net.milkbowl.vault.chat.Chat vaultChatModule =
            Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(Chat.class)).getProvider();

    @Override
    public String getPrefix(Player player) {
        String prefix = vaultChatModule.getPlayerPrefix(player);

        if (prefix == null) {
            prefix = "";
        }

        return prefix;
    }

    @Override
    public String getSuffix(Player player) {
        String suffix = vaultChatModule.getPlayerSuffix(player);

        if (suffix == null) {
            suffix = "";
        }

        return suffix;
    }

}

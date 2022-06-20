package ru.brikster.chatty.prefix;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VaultPrefixProvider implements PrefixProvider {

    private final @NotNull Chat vaultChat;

    @Override
    public String getPrefix(Player player) {
        String prefix = vaultChat.getPlayerPrefix(player);

        if (prefix == null) {
            prefix = "";
        }

        return prefix;
    }

    @Override
    public String getSuffix(Player player) {
        String suffix = vaultChat.getPlayerSuffix(player);

        if (suffix == null) {
            suffix = "";
        }

        return suffix;
    }

}

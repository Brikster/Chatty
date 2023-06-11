package ru.brikster.chatty.prefix;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class VaultPrefixProvider implements PrefixProvider {

    private final @NotNull net.milkbowl.vault.chat.Chat vaultChatModule;

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

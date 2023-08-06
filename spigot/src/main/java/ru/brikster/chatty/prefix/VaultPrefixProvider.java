package ru.brikster.chatty.prefix;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class VaultPrefixProvider implements PrefixProvider {

    private final net.milkbowl.vault.chat.Chat vaultChatModule =
            Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(Chat.class)).getProvider();

    @Override
    public String getPrefix(Player player) {
        return vaultChatModule.getPlayerPrefix(player);
    }

    @Override
    public String getSuffix(Player player) {
        return vaultChatModule.getPlayerSuffix(player);
    }

}

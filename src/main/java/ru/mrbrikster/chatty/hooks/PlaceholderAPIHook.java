package ru.mrbrikster.chatty.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook {

    public String setPlaceholders(Player player, String message) {
        return PlaceholderAPI.setPlaceholders((OfflinePlayer) player, message);
    }

}

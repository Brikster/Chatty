package ru.mrbrikster.chatty.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class PlaceholderAPIHook {

    public String setPlaceholders(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, message, Pattern.compile("[<]([^{}]+)[>]"));
    }

}

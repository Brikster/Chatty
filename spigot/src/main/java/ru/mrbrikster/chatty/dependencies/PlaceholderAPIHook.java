package ru.mrbrikster.chatty.dependencies;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public class PlaceholderAPIHook {

    public String setPlaceholders(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, message, Pattern.compile("[%]([^%]+)[%]"));
    }

    public List<String> setPlaceholders(Player player, List<String> messages) {
        return PlaceholderAPI.setPlaceholders(player, messages, Pattern.compile("[%]([^%]+)[%]"));
    }

}

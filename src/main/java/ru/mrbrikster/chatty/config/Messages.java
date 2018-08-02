package ru.mrbrikster.chatty.config;

import org.bukkit.ChatColor;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class Messages {

    private final Map<String, String> messages =
            new IdentityHashMap<>();
    private final Configuration configuration;

    private static final Function<String, String> COLORIZE
            = (string) -> string == null ? null : ChatColor.translateAlternateColorCodes('&', string);

    Messages(Configuration configuration) {
        this.configuration = configuration;

        put("chat-not-found",
                ChatColor.RED + "Applicable chat not found. You can't send the message.");
        put("cooldown",
                ChatColor.RED + "Wait for {cooldown} seconds, before send message in this chat again.");
        put("not-enough-money",
                ChatColor.RED + "You need {money} money to send message in this chat.");
        put("no-recipients",
                ChatColor.RED + "Nobody heard you.");
        put("caps-found",
                ChatColor.RED + "Caps abuse is detected in your message.");
        put("advertisement-found",
                ChatColor.RED + "Advertisement is detected in your message.");
        put("reload",
                ChatColor.GREEN + "Config successful reloaded!");
        put("no-permission",
                ChatColor.RED + "You don't have permission.");
        put("spy-off",
                ChatColor.RED + "You have been disabled spy-mode.");
        put("spy-on",
                ChatColor.GREEN + "You have been enabled spy-mode.");
        put("only-for-players",
                ChatColor.GREEN + "This command is only for players.");
    }

    public String get(String key) {
        return get(key, messages.get(key));
    }

    public String get(String key, String def) {
        return COLORIZE.apply(configuration.getNode("messages." + key)
                .getAsString(def));
    }

    private void put(String key, String message) {
        this.messages.put(key, message);
    }

}

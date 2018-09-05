package ru.mrbrikster.chatty.config;

import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class Messages {

    private final Map<String, String> messages =
            new IdentityHashMap<>();

    private static final Function<String, String> COLORIZE
            = (string) -> string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    private final Configuration localeConfiguration;

    Messages(JavaPlugin javaPlugin, Configuration configuration) {
        File localeDir = new File(javaPlugin.getDataFolder(), "locale");

        if (!localeDir.exists()) {
            if (localeDir.mkdir()) {
                URL ruUrl = getClass().getResource("/locale/ru.yml");
                URL enUrl = getClass().getResource("/locale/en.yml");
                URL deUrl = getClass().getResource("/locale/de.yml");

                try {
                    FileUtils.copyURLToFile(ruUrl, new File(localeDir, "ru.yml"));
                    FileUtils.copyURLToFile(enUrl, new File(localeDir, "en.yml"));
                    FileUtils.copyURLToFile(deUrl, new File(localeDir, "de.yml"));
                } catch (IOException e) {
                    javaPlugin.getLogger().warning("Error while copying locale files.");
                    e.printStackTrace();
                }
            }
        }

        Configuration localeConfiguration;
        switch (configuration.getNode("general.locale")
                .getAsString("en")) {
            case "ru":
                localeConfiguration = new Configuration("locale/ru.yml", javaPlugin); break;
            case "en":
                localeConfiguration = new Configuration("locale/en.yml", javaPlugin); break;
            case "de":
                localeConfiguration = new Configuration("locale/de.yml", javaPlugin); break;
            default:
                localeConfiguration = new Configuration("locale/en.yml", javaPlugin); break;
        }

        this.localeConfiguration = localeConfiguration;

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
                ChatColor.RED + "This command is only for players.");

        // Msg command
        put("msg-command.usage",
                ChatColor.RED + "Using: /{label} <player> <message>");
        put("msg-command.player-not-found",
                ChatColor.RED + "Player not found.");
        put("msg-command.cannot-message-yourself",
                ChatColor.RED + "You cannot message yourself.");
        put("msg-command.recipient-format",
                "&7{sender} &6-> &7{recipient}: &f{message}");
        put("msg-command.sender-format",
                "&7{sender} &6-> &7{recipient}: &f{message}");

        // Reply command
        put("reply-command.usage",
                ChatColor.RED + "Using: /{label} <message>");
        put("reply-command.target-not-found",
                ChatColor.RED + "Player not found.");
        put("reply-command.recipient-format",
                "&7{sender} &6-> &7{recipient}: &f{message}");
        put("reply-command.sender-format",
                "&7{sender} &6-> &7{recipient}: &f{message}");
    }

    public String get(String key) {
        return get(key, messages.get(key));
    }

    public String get(String key, String def) {
        return COLORIZE.apply(
                localeConfiguration == null
                ? def
                : localeConfiguration.getNode("messages." + key).getAsString(def));
    }

    private void put(String key, String message) {
        this.messages.put(key, message);
    }

}

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

        String localeName = configuration.getNode("general.locale")
                .getAsString("en");

        if (!localeDir.exists()) {
            localeDir.mkdir();
        }

        File localeFile = new File(localeDir, localeName + ".yml");
        if (!localeFile.exists()) {
            URL localeFileUrl = getClass().getResource("/locale/" + localeName + ".yml");

            if (localeFileUrl == null) {
                javaPlugin.getLogger().warning("Locale " + '"' + localeName + '"' + " not found. Using English locale.");

                File enLocaleFile = new File(localeDir, "en.yml");

                if (!enLocaleFile.exists()) {
                    try {
                        FileUtils.copyURLToFile(getClass().getResource("/locale/en.yml"), enLocaleFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                localeName = "en";
            } else {
                try {
                    FileUtils.copyURLToFile(localeFileUrl, localeFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.localeConfiguration = new Configuration("locale/" + localeName + ".yml", javaPlugin);

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

        put("chat-cleared",
                ChatColor.GREEN + "Chat cleared by {player}.");

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

        // Ignore command
        put("ignore-command.usage",
                ChatColor.RED + "Using: /{label} <player>");
        put("ignore-command.player-not-found",
                ChatColor.RED + "Player not found.");
        put("ignore-command.add-ignore",
                ChatColor.RED + "You are now ignoring player {player}");
        put("ignore-command.remove-ignore",
                ChatColor.GREEN + "You are no more ignoring player {player}.");
        put("ignore-command.cannot-ignore-yourself",
                ChatColor.RED + "You cannot ignore yourself.");


        put("swears-command.usage",
                ChatColor.RED + "Using: /{label} <add|remove> <word>");
        put("swears-command.add-word",
                ChatColor.GREEN + "You added word {word} to whitelist.");
    }

    public String get(String key) {
        return get(key, messages.getOrDefault(key, "&cWrong message key."));
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

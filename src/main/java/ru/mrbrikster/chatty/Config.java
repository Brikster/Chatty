package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {

    @Getter private final List<Chat> chats;
    @Getter private final boolean logEnabled;
    @Getter private final HashMap<String, String> messages;
    @Getter private final boolean spyEnabled;
    @Getter private EventPriority priority;

    Config(Main main) {
        main.saveDefaultConfig();
        main.reloadConfig();
        FileConfiguration fileConfiguration = main.getConfig();

        ConfigurationSection general = fileConfiguration.getConfigurationSection("general");
        this.priority = EventPriority.valueOf(general.getString("priority", "normal").toUpperCase());
        this.logEnabled = general.getBoolean("log");
        this.spyEnabled = general.getBoolean("spy", true);

        ConfigurationSection chats = fileConfiguration.getConfigurationSection("chats");

        this.chats = new ArrayList<>();
        for (String key : chats.getKeys(false)) {
            ConfigurationSection chat = chats.getConfigurationSection(key);
            this.chats.add(new Chat(key,
                    chat.getBoolean("enable"),
                    chat.getString("format", "{prefix}{player}{suffix}: {message}"),
                    chat.getInt("range", 100),
                    chat.getString("symbol", "")));
        }

        this.messages = new HashMap<>();
        ConfigurationSection messages = fileConfiguration.getConfigurationSection("messages");
        for (String key : messages.getKeys(false)) {
            this.messages.put(key, Utils.colorize(messages.getString(key)));
        }
    }

}

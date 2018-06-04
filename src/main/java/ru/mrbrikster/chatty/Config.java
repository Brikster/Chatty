package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import ru.mrbrikster.chatty.managers.AnnouncementsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    @Getter private final List<Chat> chats;
    @Getter private final boolean logEnabled;
    @Getter private final HashMap<String, String> messages;
    @Getter private final boolean spyEnabled;
    @Getter private final List<AnnouncementsManager.AdvancementMessage> advancementMessages;
    @Getter private final boolean announcementsEnabled;
    @Getter private final int announcementsTime;
    @Getter private EventPriority priority;
    @Getter private final String spyFormat;

    Config(Main main) {
        main.saveDefaultConfig();
        main.reloadConfig();
        FileConfiguration fileConfiguration = main.getConfig();

        ConfigurationSection general = fileConfiguration.getConfigurationSection("general");
        this.priority = EventPriority.valueOf(general.getString("priority", "normal").toUpperCase());
        this.logEnabled = general.getBoolean("log");
        this.spyEnabled = general.getBoolean("spy.enable", true);
        this.spyFormat = general.getString("spy.format", "&6[Spy] &r{format}");

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

        ConfigurationSection announcements = fileConfiguration.getConfigurationSection("announcements");

        this.announcementsEnabled = announcements.getBoolean("enable", false);

        this.announcementsTime = announcements.getInt("time", 60);
        this.advancementMessages = new ArrayList<>();

        if (announcementsEnabled) {
            for (Map<?, ?> list : announcements.getMapList("list"))
                this.advancementMessages.add(new AnnouncementsManager.AdvancementMessage(list, main));
        }
    }

}

package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import ru.mrbrikster.chatty.commands.CommandGroup;
import ru.mrbrikster.chatty.managers.AlertsManager;
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
    @Getter private final boolean alertsEnabled;
    @Getter private final ArrayList<AlertsManager.AlertList> alertsLists;
    @Getter private final boolean antiAdsEnabled;
    @Getter private final List<String> adsWhitelist;
    @Getter private final List<CommandGroup> commandGroups;
    @Getter private EventPriority priority;
    @Getter private final String spyFormat;

    Config(Main main) {
        main.saveDefaultConfig();
        main.reloadConfig();
        FileConfiguration fileConfiguration = main.getConfig();

        // General
        ConfigurationSection general = fileConfiguration.getConfigurationSection("general");
        this.priority = EventPriority.valueOf(general.getString("priority", "normal").toUpperCase());
        this.logEnabled = general.getBoolean("log");
        this.spyEnabled = general.getBoolean("spy.enable");
        this.spyFormat = general.getString("spy.format", "&6[Spy] &r{format}");

        // Chats
        ConfigurationSection chats = fileConfiguration.getConfigurationSection("chats");

        this.chats = new ArrayList<>();
        for (String key : chats.getKeys(false)) {
            ConfigurationSection chat = chats.getConfigurationSection(key);
            this.chats.add(new Chat(key,
                    chat.getBoolean("enable"),
                    chat.getString("format", "{prefix}{player}{suffix}: {message}"),
                    chat.getInt("range", -1),
                    chat.getString("symbol", ""),
                    chat.getBoolean("permission", true),
                    chat.getLong("cooldown", -1),
                    chat.getInt("money", 0)));
        }

        // I18n
        this.messages = new HashMap<>();
        ConfigurationSection messages = fileConfiguration.getConfigurationSection("messages");
        for (String key : messages.getKeys(false)) {
            this.messages.put(key, Utils.colorize(messages.getString(key)));
        }

        // Command groups
        this.commandGroups = new ArrayList<>();

        ConfigurationSection commandGroupsSection = fileConfiguration.getConfigurationSection("commands");
        if (commandGroupsSection != null) {
            for (String key : commandGroupsSection.getKeys(false)) {
                ConfigurationSection section = commandGroupsSection.getConfigurationSection(key);
                this.commandGroups.add(
                        new CommandGroup(
                                key.toLowerCase(),
                                section.getStringList("triggers"),
                                section.getBoolean("block"),
                                section.getString("message"),
                                section.getLong("cooldown", -1)));
            }
        }

        // Alerts
        ConfigurationSection alerts = fileConfiguration.getConfigurationSection("alerts");

        this.alertsEnabled = alerts.getBoolean("enable");
        this.alertsLists = new ArrayList<>();

        ConfigurationSection listsSection = alerts.getConfigurationSection("lists");
        if (listsSection != null) {
            for (String key : listsSection.getKeys(false)) {
                ConfigurationSection section = listsSection.getConfigurationSection(key);
                this.alertsLists.add(
                        new AlertsManager.AlertList(
                                key.toLowerCase(),
                                section.getInt("time", 5),
                                section.getString("prefix", ""),
                                section.getStringList("messages"),
                                section.getBoolean("permission", true)));
            }
        }

        // Anti-ads
        this.antiAdsEnabled = fileConfiguration.getBoolean("anti-ads.enable");
        this.adsWhitelist = fileConfiguration.getStringList("anti-ads.whitelist");

        // Announcements
        ConfigurationSection announcements = fileConfiguration.getConfigurationSection("announcements");

        this.announcementsEnabled = announcements.getBoolean("enable");
        this.announcementsTime = announcements.getInt("time", 60);
        this.advancementMessages = new ArrayList<>();

        if (announcementsEnabled) {
            for (Map<?, ?> list : announcements.getMapList("list"))
                this.advancementMessages.add(new AnnouncementsManager.AdvancementMessage(list, main));

            if (main.getAnnouncementsManager() != null)
                main.getAnnouncementsManager().reset();
        }
    }

}

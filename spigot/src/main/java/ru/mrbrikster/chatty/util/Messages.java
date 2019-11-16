package ru.mrbrikster.chatty.util;

import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.mrbrikster.baseplugin.config.BukkitConfiguration;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Function;

public class Messages {

    private static final Function<String, String> COLORIZE = (string) -> string == null ? null : ChatColor.translateAlternateColorCodes('&', string);

    private Configuration localeConfiguration;
    private Configuration inJarConfiguration;

    public Messages(BukkitBasePlugin bukkitBasePlugin, Configuration configuration) {
        File localeDir = new File(bukkitBasePlugin.getDataFolder(), "locale");

        String localeName = configuration.getNode("general.locale")
                .getAsString("en");

        if (!localeDir.exists()) {
            localeDir.mkdir();
        }

        File localeFile = new File(localeDir, localeName + ".yml");
        if (!localeFile.exists()) {
            URL localeFileUrl = getClass().getResource("/locale/" + localeName + ".yml");

            if (localeFileUrl == null) {
                bukkitBasePlugin.getLogger().warning("Locale " + '"' + localeName + '"' + " not found. Using \"en\" locale.");

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

        this.localeConfiguration = bukkitBasePlugin.getConfiguration("locale/" + localeName + ".yml");
        this.inJarConfiguration = new BukkitConfiguration(YamlConfiguration.loadConfiguration(new InputStreamReader(bukkitBasePlugin.getClass().getResourceAsStream("/locale/en.yml"))));
    }

    public String get(String key) {
        return get(key, inJarConfiguration.getNode("messages." + key).getAsString("&cLocale message not found."));
    }

    public String get(String key, String def) {
        return COLORIZE.apply(localeConfiguration == null ? def : localeConfiguration.getNode("messages." + key).getAsString(def));
    }

}

package ru.mrbrikster.chatty.util;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.mrbrikster.baseplugin.config.BukkitConfiguration;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Function;

public class Messages {

    private static final Function<String, String> COLORIZE = (string) -> string == null
            ? null
            : TextUtil.stylish(string);

    private final Configuration localeConfiguration;
    private final Configuration inJarConfiguration;

    public Messages(Chatty chatty) {
        File localeDir = new File(chatty.getDataFolder(), "locale");

        String localeName = chatty.getExact(Configuration.class).getNode("general.locale")
                .getAsString("en");

        if (!localeDir.exists()) {
            if (!localeDir.mkdir()) {
                chatty.getLogger().warning("Cannot create \"locale\" directory");
            }
        }

        File localeFile = new File(localeDir, localeName + ".yml");
        if (!localeFile.exists()) {
            URL localeFileUrl = getClass().getResource("/locale/" + localeName + ".yml");

            if (localeFileUrl == null) {
                chatty.getLogger().warning("Locale " + '"' + localeName + '"' + " not found. Using \"en\" locale.");

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

        this.localeConfiguration = chatty.getConfiguration("locale/" + localeName + ".yml");
        this.inJarConfiguration = new BukkitConfiguration(YamlConfiguration.loadConfiguration(new InputStreamReader(chatty.getClass().getResourceAsStream("/locale/en.yml"))));
    }

    public String get(String key) {
        return get(key, inJarConfiguration.getNode("messages." + key).getAsString("&cLocale message not found."));
    }

    public String get(String key, String def) {
        return TextUtil.stylish(localeConfiguration == null
                ? def
                : localeConfiguration.getNode("messages." + key).getAsString(def));
    }

}

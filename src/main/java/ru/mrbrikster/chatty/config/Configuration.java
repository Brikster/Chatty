package ru.mrbrikster.chatty.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Configuration {

    private final File file;
    private final YamlConfiguration configuration;

    public Configuration(JavaPlugin javaPlugin) {
        javaPlugin.saveDefaultConfig();

        this.file = new File(javaPlugin.getDataFolder(), "config.yml");
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public ConfigurationNode getNode(String path) {
        if (configuration.contains(path)
                || configuration.getConfigurationSection(path) != null)
            return new ConfigurationNodeImpl(configuration, path);

        return new EmptyConfigurationNode();
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

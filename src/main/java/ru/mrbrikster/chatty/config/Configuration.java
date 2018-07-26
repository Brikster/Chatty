package ru.mrbrikster.chatty.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private final File file;
    private YamlConfiguration configuration;
    private List<ReloadHandler> reloadHandlers = new ArrayList<>();

    public Configuration(JavaPlugin javaPlugin) {
        javaPlugin.saveDefaultConfig();

        this.file = new File(javaPlugin.getDataFolder(), "config.yml");
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public ConfigurationNode getNode(String path) {
        if (configuration.contains(path)
                || configuration.getConfigurationSection(path) != null)
            return new ConfigurationNodeImpl(configuration, path);

        return new EmptyConfigurationNode(path);
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.configuration = YamlConfiguration.loadConfiguration(file);
        reloadHandlers.forEach(ReloadHandler::onConfigurationReload);
    }

    public void registerReloadHandler(ReloadHandler reloadHandler) {
        this.reloadHandlers.add(reloadHandler);
    }

    public interface ReloadHandler {

        public void onConfigurationReload();

    }

}

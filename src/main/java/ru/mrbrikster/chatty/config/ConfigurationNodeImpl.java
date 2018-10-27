package ru.mrbrikster.chatty.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationNodeImpl implements ConfigurationNode {

    private final FileConfiguration configuration;
    private final String path;

    ConfigurationNodeImpl(FileConfiguration configuration, String path) {
        this.configuration = configuration;
        this.path = path;
    }

    @Override
    public String getName() {
        String[] path = this.path.split("\\.");

        return path[path.length - 1];
    }

    @Override
    public Object get(Object def) {
        return configuration.get(path, def);
    }

    @Override
    public boolean getAsBoolean(boolean def) {
        return configuration.getBoolean(path, def);
    }

    @Override
    public String getAsString(String def) {
        return configuration.getString(path, def);
    }

    @Override
    public long getAsLong(long def) {
        return configuration.getLong(path, def);
    }

    @Override
    public int getAsInt(int def) {
        return configuration.getInt(path, def);
    }

    @Override
    public List getAsList(List def) {
        return configuration.getList(path, def);
    }

    @Override
    public List<Map<?, ?>> getAsMapList() {
        return configuration.getMapList(path);
    }

    @Override
    public List<String> getAsStringList() {
        return configuration.getStringList(path);
    }

    @Override
    public ConfigurationSection getAsConfigurationSection() {
        return configuration.getConfigurationSection(path);
    }

    @Override
    public ConfigurationNode getNode(String path) {
        if (configuration.contains(this.path + "." + path)
                || configuration.getConfigurationSection(this.path + "." + path) != null)
            return new ConfigurationNodeImpl(configuration, this.path + "." + path);

        return new EmptyConfigurationNode(path);
    }

    @Override
    public List<ConfigurationNode> getChildNodes() {
        ConfigurationSection section = getAsConfigurationSection();

        if (section == null) {
            return Collections.emptyList();
        }

        return section.getKeys(false).stream()
                .map(key -> new ConfigurationNodeImpl(configuration, path + "." + key)).collect(Collectors.toList());
    }

    @Override
    public void set(Object value) {
        configuration.set(path, value);
    }

    @Override
    public boolean isEmpty() {
        return !configuration.contains(path);
    }

}

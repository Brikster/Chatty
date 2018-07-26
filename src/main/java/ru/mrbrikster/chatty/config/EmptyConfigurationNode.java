package ru.mrbrikster.chatty.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmptyConfigurationNode implements ConfigurationNode {

    private final String path;

    public EmptyConfigurationNode(String path) {
        this.path = path;
    }

    @Override
    public String getName() {
        String[] path = this.path.split(".");

        return path[path.length - 1];
    }

    @Override
    public Object get(Object def) {
        return def;
    }

    @Override
    public boolean getAsBoolean(boolean def) {
        return def;
    }

    @Override
    public String getAsString(String def) {
        return def;
    }

    @Override
    public long getAsLong(long def) {
        return def;
    }

    @Override
    public int getAsInt(int def) {
        return def;
    }

    @Override
    public List getAsList(List def) {
        return def;
    }

    @Override
    public List<Map<?, ?>> getAsMapList() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getAsStringList() {
        return new ArrayList<>();
    }

    @Override
    public ConfigurationSection getAsConfigurationSection() {
        return null;
    }

    @Override
    public ConfigurationNode getNode(String path) {
        return new EmptyConfigurationNode(this.path + "." + path);
    }

    @Override
    public List<ConfigurationNode> getChildNodes() {
        return Collections.emptyList();
    }

    @Override
    public void set(Object value) {
        throw new UnsupportedOperationException("Node is empty");
    }

}

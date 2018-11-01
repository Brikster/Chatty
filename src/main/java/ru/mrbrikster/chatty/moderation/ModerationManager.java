package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;

public class ModerationManager {

    private final JavaPlugin javaPlugin;
    private final Configuration configuration;
    @Getter private boolean capsModerationEnabled;
    @Getter private boolean advertisementModerationEnabled;
    @Getter private boolean swearModerationEnabled;

    public ModerationManager(JavaPlugin javaPlugin, Configuration configuration) {
        this.javaPlugin = javaPlugin;
        this.configuration = configuration;

        init();
        configuration.registerReloadHandler(this::reload);
    }

    private void init() {
        ConfigurationNode moderationNode =
                configuration.getNode("moderation");

        this.capsModerationEnabled = moderationNode.getNode("caps.enable")
                .getAsBoolean(false);

        this.advertisementModerationEnabled = moderationNode.getNode("advertisement.enable")
                .getAsBoolean(false);

        this.swearModerationEnabled = moderationNode.getNode("swear.enable")
                .getAsBoolean(false);

        if (swearModerationEnabled) {
            SwearModerationMethod.init(javaPlugin);
        }
    }

    private void reload() {
        init();
    }

    public CapsModerationMethod getCapsMethod(String message) {
        return new CapsModerationMethod(configuration.getNode("moderation.caps"), message);
    }

    public AdvertisementModerationMethod getAdvertisementMethod(String message) {
        return new AdvertisementModerationMethod(configuration.getNode("moderation.advertisement"), message);
    }

    public SwearModerationMethod getSwearMethod(String message) {
        return new SwearModerationMethod(configuration.getNode("moderation.swear"), message);
    }

}

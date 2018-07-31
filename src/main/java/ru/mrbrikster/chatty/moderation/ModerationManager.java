package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.config.ConfigurationNode;

public class ModerationManager {

    private final Configuration configuration;
    @Getter
    private boolean capsModerationEnabled;
    @Getter
    private boolean advertisementModerationEnabled;

    public ModerationManager(Configuration configuration) {
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

}

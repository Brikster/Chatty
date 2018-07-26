package ru.mrbrikster.chatty.moderation;

import ru.mrbrikster.chatty.config.ConfigurationNode;

public abstract class ModerationMethod {

    protected final String message;
    protected final ConfigurationNode configurationNode;

    public ModerationMethod(ConfigurationNode configurationNode, String message) {
        this.configurationNode = configurationNode;
        this.message = message;
    }

    public abstract String getEditedMessage();

    public abstract boolean isPassed();

}

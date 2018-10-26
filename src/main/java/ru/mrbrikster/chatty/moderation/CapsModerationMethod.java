package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.chatty.config.ConfigurationNode;

public class CapsModerationMethod extends ModerationMethod {

    private final int procent;
    private final int length;
    @Getter private final boolean useBlock;

    CapsModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);

        this.useBlock = configurationNode.getNode("block").getAsBoolean(true);
        this.procent = configurationNode.getNode("procent").getAsInt(80);
        this.length = configurationNode.getNode("length").getAsInt(6);
    }

    @Override
    public String getEditedMessage() {
        return message.toLowerCase();
    }

    @Override
    public boolean isBlocked() {
        return message.length() >= length && getProcent() >= procent;
    }

    private double getProcent() {
        int codePoint, length = 0, capsLength = 0;
        for (char c : message.toCharArray()) {
            codePoint = (int) c;
            if (Character.isLetter(codePoint)) {
                length++;
                if (codePoint == Character.toUpperCase(codePoint)) {
                    capsLength++;
                }
            }
        }

        return (double) capsLength / (double) length * 100;
    }

}

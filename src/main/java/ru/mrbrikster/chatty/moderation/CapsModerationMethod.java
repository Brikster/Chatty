package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.chatty.config.ConfigurationNode;

public class CapsModerationMethod extends ModerationMethod {

    private final int procent;
    private final int length;
    @Getter private final boolean block;

    CapsModerationMethod(ConfigurationNode configurationNode, String message) {
        super(configurationNode, message);

        this.block = configurationNode.getNode("block").getAsBoolean(true);
        this.procent = configurationNode.getNode("procent").getAsInt(80);
        this.length = configurationNode.getNode("length").getAsInt(6);
    }

    @Override
    public String getEditedMessage() {
        return block ? message : message.toLowerCase();
    }

    @Override
    public boolean isPassed() {
        return message.length() < length || getProcent() < procent;
    }

    private double getProcent() {
        String[] words = message.split(" ");
        int length = 0, capsLength = 0;

        for (String word : words) {
            length += word.length();

            for (char c : word.toCharArray()) {
                if (!isNumber(c) && c == Character.toUpperCase(c)) {
                    capsLength++;
                }
            }
        }

        return ((double) capsLength / (double) length) * 100;
    }

    private boolean isNumber(char c) {
        try {
            Integer.parseInt(String.valueOf(c));
        } catch (NumberFormatException numberFormatException) {
            return false;
        }

        return true;
    }

}

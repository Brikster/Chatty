package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.chatty.config.ConfigurationNode;

public class CapsModerationMethod extends ModerationMethod {

    private final int procent;
    private final int length;
    @Getter private final boolean block;

    CapsModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);

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
        String messageWithoutChars = message
                .replaceAll("[^a-zA-Zа-яА-Я]", "");

        if (messageWithoutChars.isEmpty())
            return 0;

        String[] words = messageWithoutChars.split(" ");
        int length = 0, capsLength = 0;

        for (String word : words) {
            length += word.length();

            for (char c : word.toCharArray()) {
                if (c == Character.toUpperCase(c)) {
                    capsLength++;
                }
            }
        }

        return ((double) capsLength / (double) length) * 100;
    }

}

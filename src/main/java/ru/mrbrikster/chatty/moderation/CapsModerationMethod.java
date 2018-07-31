package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.chatty.config.ConfigurationNode;

public class CapsModerationMethod extends ModerationMethod {

    private final int procent;
    private final int length;
    @Getter private final boolean useBlock;

    CapsModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);

        this.useBlock = configurationNode.getNode("useBlock").getAsBoolean(true);
        this.procent = configurationNode.getNode("procent").getAsInt(80);
        this.length = configurationNode.getNode("length").getAsInt(6);
    }

    @Override
    public String getEditedMessage() {
        return useBlock ? message : message.toLowerCase();
    }

    @Override
    public boolean isBlocked() {
        return message.length() >= length && getProcent() >= procent;
    }

    private double getProcent() {
        String messageWithoutChars = message
                .replaceAll("[^a-zA-Zа-яА-Я]", "");

        if (messageWithoutChars.isEmpty())
            return 0;

        int length = 0, capsLength = 0;

        for (String word : messageWithoutChars.split(" ")) {
            length += word.length();

            for (char c : word.toCharArray())
                if (c == Character.toUpperCase(c)) capsLength++;
        }

        return (double) capsLength / (double) length * 100;
    }

}

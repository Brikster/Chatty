package ru.mrbrikster.chatty.json;

import lombok.Getter;
import org.bukkit.ChatColor;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

public class LegacyMessagePart implements MessagePart {

    @Getter
    private final String text;
    private final boolean colorize;

    LegacyMessagePart(String text) {
        this.text = text;
        this.colorize = true;
    }

    public LegacyMessagePart(String text, boolean colorize) {
        this.text = text;
        this.colorize = colorize;
    }

    @Override
    public FancyMessage append(FancyMessage fancyMessage) {
        LegacyConverter.getMessageParts(fancyMessage.getLastColors() + (colorize ? ChatColor.translateAlternateColorCodes('&', text) : text)).forEach(fancyMessage::then);
        return fancyMessage;
    }

}

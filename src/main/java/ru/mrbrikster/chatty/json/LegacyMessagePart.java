package ru.mrbrikster.chatty.json;

import lombok.Getter;
import org.bukkit.ChatColor;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

public class LegacyMessagePart implements MessagePart {

    @Getter
    private final String text;

    public LegacyMessagePart(String text) {
        this.text = text;
    }

    @Override
    public FancyMessage append(FancyMessage fancyMessage) {
        LegacyConverter.getMessageParts(fancyMessage.getLastColors() + ChatColor.translateAlternateColorCodes('&', text)).forEach(fancyMessage::then);
        return fancyMessage;
    }

}

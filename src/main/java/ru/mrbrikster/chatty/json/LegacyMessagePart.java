package ru.mrbrikster.chatty.json;

import lombok.Getter;
import ru.mrbrikster.chatty.fanciful.FancyMessage;

public class LegacyMessagePart implements MessagePart {

    @Getter private final String text;

    public LegacyMessagePart(String text) {
        this.text = text;
    }

    @Override
    public FancyMessage append(FancyMessage fancyMessage) {
        return fancyMessage.then(fancyMessage.getLastColors() + text);
    }

}

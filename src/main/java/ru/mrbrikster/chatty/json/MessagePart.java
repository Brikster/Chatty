package ru.mrbrikster.chatty.json;

import ru.mrbrikster.chatty.fanciful.FancyMessage;

public interface MessagePart {

    FancyMessage append(FancyMessage fancyMessage);

}

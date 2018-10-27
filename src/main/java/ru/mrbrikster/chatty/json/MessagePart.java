package ru.mrbrikster.chatty.json;

import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

public interface MessagePart {

    FancyMessage append(FancyMessage fancyMessage);

}

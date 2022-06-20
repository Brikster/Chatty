package ru.brikster.chatty.chat.construct;

import net.kyori.adventure.text.Component;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

public interface MessageConstructor {

    Component construct(MessageContext<Component> context);

}

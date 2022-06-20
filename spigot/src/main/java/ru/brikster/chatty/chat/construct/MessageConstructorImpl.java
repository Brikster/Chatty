package ru.brikster.chatty.chat.construct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

public class MessageConstructorImpl implements MessageConstructor {

    @Override
    public Component construct(MessageContext<Component> context) {
        return context.getFormat()
                .replaceText(TextReplacementConfig.builder()
                        .matchLiteral("{player}")
                        .replacement(context.getSender().getDisplayName())
                        .build())
                .replaceText(TextReplacementConfig.builder()
                        .matchLiteral("{message}")
                        .replacement(context.getMessage())
                        .build());
    }

}

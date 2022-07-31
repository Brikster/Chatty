package ru.brikster.chatty.chat.construct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

public class MessageConstructorImpl implements MessageConstructor {

    private static final String PLAYER_FORMAT_PLACEHOLDER = "<player>";
    private static final String MESSAGE_FORMAT_PLACEHOLDER = "<message>";

    @Override
    public Component construct(MessageContext<Component> context) {
        return context.getFormat()
                .replaceText(TextReplacementConfig.builder()
                        .matchLiteral(PLAYER_FORMAT_PLACEHOLDER)
                        .replacement(context.getSender().getDisplayName())
                        .build())
                .replaceText(TextReplacementConfig.builder()
                        .matchLiteral(MESSAGE_FORMAT_PLACEHOLDER)
                        .replacement(context.getMessage())
                        .build());
    }

}

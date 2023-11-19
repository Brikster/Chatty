package ru.brikster.chatty.chat.message.transform.intermediary;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.decorations.PlayerDecorationsFormatter;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class IntermediateMessageTransformerImpl implements IntermediateMessageTransformer {

    @Inject private PlayerDecorationsFormatter decorationsFormatter;

    @Override
    public @NotNull MessageTransformResult<Component> handle(MessageContext<String> context) {
        Component messageComponent = decorationsFormatter.formatMessageWithDecorations(context.getSender(), context.getMessage());
        return MessageTransformResultBuilder
                .<Component>fromContext(context)
                .withMessage(messageComponent)
                .build();
    }

}

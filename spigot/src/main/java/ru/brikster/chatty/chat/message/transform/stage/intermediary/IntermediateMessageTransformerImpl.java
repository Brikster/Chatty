package ru.brikster.chatty.chat.message.transform.stage.intermediary;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;

public final class IntermediateMessageTransformerImpl implements IntermediateMessageTransformer {

    @Inject
    private ComponentStringConverter converter;

    @Override
    public @NotNull MessageTransformResult<Component> handle(MessageContext<String> context) {
        Component message = converter.stringToComponent(context.getMessage());
        return MessageTransformResultBuilder
                .<Component>fromContext(context)
                .withMessage(message)
                .build();
    }

}

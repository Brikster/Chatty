package ru.brikster.chatty.chat.message.strategy.impl;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;

public abstract class ComponentTransformerFormatMessageTransformStrategy<T> implements MessageTransformStrategy<T, T> {

    private final ComponentTransformer<SinglePlayerTransformContext> formatTransformer;

    protected ComponentTransformerFormatMessageTransformStrategy(@NotNull ComponentTransformer<SinglePlayerTransformContext> formatTransformer) {
        this.formatTransformer = formatTransformer;
    }

    @Override
    public @NotNull final Result<T> handle(final MessageContext<T> context) {
        MessageContext<T> newContext = new MessageContextImpl<>(context);
        Component newFormat = formatTransformer.transform(context.getFormat(), SinglePlayerTransformContext.of(context.getSender()));
        boolean formatUpdated = !newFormat.equals(context.getFormat());
        newContext.setFormat(newFormat);

        return ResultImpl.<T>builder()
                .newContext(newContext)
                .formatUpdated(formatUpdated)
                .build();
    }

}

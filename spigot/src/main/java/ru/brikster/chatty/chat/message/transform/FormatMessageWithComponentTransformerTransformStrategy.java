package ru.brikster.chatty.chat.message.transform;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;

public abstract class FormatMessageWithComponentTransformerTransformStrategy<MessageT> implements MessageTransformStrategy<MessageT> {

    private final ComponentTransformer<SinglePlayerTransformContext> formatTransformer;

    protected FormatMessageWithComponentTransformerTransformStrategy(@NotNull ComponentTransformer<SinglePlayerTransformContext> formatTransformer) {
        this.formatTransformer = formatTransformer;
    }

    @Override
    public @NotNull final MessageTransformResult<MessageT> handle(final MessageContext<MessageT> context, @Nullable Player target) {
        Component newFormat = formatTransformer.transform(context.getFormat(), SinglePlayerTransformContext.of(context.getSender()));
        return MessageTransformResultBuilder.<MessageT>fromContext(context)
                .withFormat(newFormat)
                .build();
    }

}

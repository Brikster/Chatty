package ru.brikster.chatty.chat.message.transform;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.TransformContext;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;

import java.util.function.Function;

public abstract class AbstractComponentTransformerStrategy<MessageT, TransformContextT extends TransformContext> implements MessageTransformStrategy<MessageT> {

    private final ComponentTransformer<TransformContextT> formatTransformer;
    private final Function<MessageContext<MessageT>, TransformContextT> transformContextFunction;

    protected AbstractComponentTransformerStrategy(@NotNull ComponentTransformer<TransformContextT> formatTransformer,
                                                   @NotNull Function<MessageContext<MessageT>, TransformContextT> transformContextFunction) {
        this.formatTransformer = formatTransformer;
        this.transformContextFunction = transformContextFunction;
    }

    @Override
    public @NotNull final MessageTransformResult<MessageT> handle(final MessageContext<MessageT> context) {
        TransformContextT transformContext = transformContextFunction.apply(context);
        Component newFormat = formatTransformer.transform(context.getFormat(), transformContext);
        return MessageTransformResultBuilder.<MessageT>fromContext(context)
                .withFormat(newFormat)
                .build();
    }

}

package ru.brikster.chatty.chat.message.transform.stage.middle;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.LinkParserComponentTransformer;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class LinkParserTransformStrategy implements MessageTransformStrategy<Component> {

    @Inject private LinkParserComponentTransformer linkParserComponentTransformer;

    @Override
    public @NotNull MessageTransformResult<Component> handle(MessageContext<Component> context) {
        if (!context.getChat().isParseLinks()) {
            return MessageTransformResultBuilder.<Component>fromContext(context).build();
        }
        return MessageTransformResultBuilder.<Component>fromContext(context)
                .withMessage(linkParserComponentTransformer.transform(
                        context.getMessage(),
                        SinglePlayerTransformContext.of(context.getSender())))
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.MIDDLE;
    }

}

package ru.brikster.chatty.chat.message.transform.stage.late.prefix;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.prefix.PrefixComponentTransformer;
import ru.brikster.chatty.chat.message.transform.AbstractComponentTransformerStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class PrefixStrategy extends AbstractComponentTransformerStrategy<Component, SinglePlayerTransformContext> {

    @Inject
    private PrefixStrategy(PrefixComponentTransformer prefixComponentTransformer) {
        super(prefixComponentTransformer, context -> SinglePlayerTransformContext.of(context.getSender()));
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

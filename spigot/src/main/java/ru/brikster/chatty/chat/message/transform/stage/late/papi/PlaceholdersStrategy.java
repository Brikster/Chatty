package ru.brikster.chatty.chat.message.transform.stage.late.papi;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.message.transform.AbstractComponentTransformerStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class PlaceholdersStrategy extends AbstractComponentTransformerStrategy<Component, SinglePlayerTransformContext> {

    @Inject
    public PlaceholdersStrategy(PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        super(placeholdersComponentTransformer, context -> SinglePlayerTransformContext.of(context.getSender()));
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

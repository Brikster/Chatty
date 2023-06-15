package ru.brikster.chatty.chat.message.transform.stage.late.papi;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.strategy.stage.LateMessageTransformStrategy;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.message.transform.FormatMessageWithComponentTransformerTransformStrategy;

import javax.inject.Inject;

public final class PlaceholdersMessageTransformStrategy extends FormatMessageWithComponentTransformerTransformStrategy<String>
        implements LateMessageTransformStrategy {

    @Inject
    public PlaceholdersMessageTransformStrategy(PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        super(placeholdersComponentTransformer);
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

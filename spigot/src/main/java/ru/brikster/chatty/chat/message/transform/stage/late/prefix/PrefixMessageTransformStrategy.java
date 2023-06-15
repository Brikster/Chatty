package ru.brikster.chatty.chat.message.transform.stage.late.prefix;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.strategy.stage.LateMessageTransformStrategy;
import ru.brikster.chatty.chat.component.impl.PrefixComponentTransformer;
import ru.brikster.chatty.chat.message.transform.FormatMessageWithComponentTransformerTransformStrategy;

import javax.inject.Inject;

public final class PrefixMessageTransformStrategy extends FormatMessageWithComponentTransformerTransformStrategy<String>
        implements LateMessageTransformStrategy {

    @Inject
    private PrefixMessageTransformStrategy(PrefixComponentTransformer prefixComponentTransformer) {
        super(prefixComponentTransformer);
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

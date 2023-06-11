package ru.brikster.chatty.chat.message.strategy.impl.papi;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.chat.message.strategy.impl.ComponentTransformerFormatMessageTransformStrategy;

public final class PlaceholderApiMessageTransformStrategy extends ComponentTransformerFormatMessageTransformStrategy<String> {

    private static final PlaceholderApiMessageTransformStrategy INSTANCE = new PlaceholderApiMessageTransformStrategy();

    private PlaceholderApiMessageTransformStrategy() {
        super(PlaceholderApiComponentTransformer.instance());
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    public static MessageTransformStrategy<String, String> instance() {
        return INSTANCE;
    }

}

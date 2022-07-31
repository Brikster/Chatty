package ru.brikster.chatty.chat.handle.strategy.impl.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;

public class PlaceholderApiMessageTransformStrategy<T> implements MessageTransformStrategy<String, String> {

    private static final GsonComponentSerializer COMPONENT_SERIALIZER = GsonComponentSerializer.gson();

    @Override
    public @NotNull Result<String> handle(MessageContext<String> context) {
        String formatAsString = COMPONENT_SERIALIZER.serialize(context.getFormat());
        formatAsString = PlaceholderAPI.setPlaceholders(context.getSender(), formatAsString);

        MessageContext<String> newContext = new MessageContextImpl<>(context);
        newContext.setFormat(COMPONENT_SERIALIZER.deserialize(formatAsString));

        return ResultImpl.<String>builder()
                .newContext(newContext)
                .formatUpdated(true)
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

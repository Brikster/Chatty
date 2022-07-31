package ru.brikster.chatty.convert.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;

public class MiniMessageConverter implements ComponentConverter {

    private final MessageConverter preConverter = new LegacyToMiniMessageConverter();

    private final MiniMessage miniMessage = MiniMessage
            .builder()
            .preProcessor(message ->
                    preConverter.convert(message.replaceFirst("\n$", "")))
            .build();

    @Override
    public @NotNull Component convert(@NotNull String message) {
        return miniMessage.deserialize(message);
    }

}

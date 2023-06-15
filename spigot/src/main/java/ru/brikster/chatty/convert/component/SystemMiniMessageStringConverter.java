package ru.brikster.chatty.convert.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;

public final class SystemMiniMessageStringConverter implements ComponentStringConverter {

    private final MessageConverter preConverter = new LegacyToMiniMessageConverter();

    private final MiniMessage miniMessage = MiniMessage
            .builder()
            .preProcessor(message ->
                    preConverter.convert(message.replaceFirst("\n$", "")))
            .build();

    private final Cache<Component, String> componentStringCache = CacheBuilder.newBuilder()
            .weakKeys()
            .build();

    @Override
    public @NotNull Component stringToComponent(@NotNull String value) {
        Component converted = miniMessage.deserialize(value);
        componentStringCache.put(converted, value);
        return converted;
    }

    @Override
    public @NotNull String componentToString(@NotNull Component value) {
        String stringRepresentation = componentStringCache.getIfPresent(value);
        return stringRepresentation == null ? miniMessage.serialize(value) : stringRepresentation;
    }

}

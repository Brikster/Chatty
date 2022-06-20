package ru.brikster.chatty.convert.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;

public class MiniMessageConverter implements ComponentConverter {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MessageConverter preConverter = new LegacyToMiniMessageConverter();

    @Override
    public @NotNull Component convert(@NotNull String message) {
        String miniMessageString = preConverter.convert(message);
        return miniMessage.deserialize(miniMessageString);
    }

}

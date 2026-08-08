package ru.brikster.chatty.chat;

import lombok.Value;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.ChatStyle;

import java.util.Set;

@Value
public class SenderFormat {

    @NotNull String id;
    int priority;
    @NotNull Component format;
    @NotNull String messageFormat;
    @NotNull Set<ChatStyle> styles;

}

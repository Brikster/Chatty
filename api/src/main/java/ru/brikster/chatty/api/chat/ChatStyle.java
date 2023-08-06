package ru.brikster.chatty.api.chat;

import lombok.Value;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;

@Value
@Accessors(fluent = true)
public class ChatStyle {
    String id;
    Component format;
    int priority;
}

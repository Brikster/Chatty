package ru.brikster.chatty.proxy.data;

import lombok.Value;
import net.kyori.adventure.sound.Sound;

import java.util.Map;
import java.util.UUID;

@Value
public class ChatMessage {
    UUID clientId;
    String chatId;
    String noStyleComponentJson;
    Map<String, ChatStyle> styleComponentJsonMap;
    Sound sound;
}

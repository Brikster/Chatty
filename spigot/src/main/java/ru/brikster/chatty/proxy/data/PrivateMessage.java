package ru.brikster.chatty.proxy.data;

import lombok.Value;
import net.kyori.adventure.sound.Sound;

import java.util.UUID;

@Value
public class PrivateMessage {
    UUID clientId;
    String targetName;
    String componentJson;
    String spyComponentJson;
    String logMessage;
    Sound sound;
}

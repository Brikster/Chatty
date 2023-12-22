package ru.brikster.chatty.proxy.data;

import lombok.Value;

import java.util.UUID;

@Value
public class ProxyPlayer {
    String username;
    UUID uuid;
}

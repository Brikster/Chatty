package ru.brikster.chatty.chat.registry;

import ru.brikster.chatty.api.chat.Chat;

import java.util.Map;

public interface ChatRegistry {

    void register(String id, Chat chat);

    void unregisterAll();

    Map<String, Chat> getChats();

}

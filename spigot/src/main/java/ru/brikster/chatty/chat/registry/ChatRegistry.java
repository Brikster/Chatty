package ru.brikster.chatty.chat.registry;

import ru.brikster.chatty.api.chat.Chat;

import java.util.Collection;

public interface ChatRegistry {

    void register(Chat chat);

    void unregisterAll();

    Collection<Chat> getChats();

}

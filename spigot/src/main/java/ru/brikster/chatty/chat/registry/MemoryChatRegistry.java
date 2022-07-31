package ru.brikster.chatty.chat.registry;

import ru.brikster.chatty.api.chat.Chat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MemoryChatRegistry implements ChatRegistry {

    private final Set<Chat> chats = new HashSet<>();

    @Override
    public void register(Chat chat) {
        this.chats.add(chat);
    }

    @Override
    public void unregisterAll() {
        chats.clear();
    }

    @Override
    public Collection<Chat> getChats() {
        return Collections.unmodifiableSet(chats);
    }

}

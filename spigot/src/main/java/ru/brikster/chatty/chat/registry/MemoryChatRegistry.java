package ru.brikster.chatty.chat.registry;

import ru.brikster.chatty.api.chat.Chat;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MemoryChatRegistry implements ChatRegistry {

    private final Map<String, Chat> chats = new ConcurrentHashMap<>();

    @Override
    public void register(String id, Chat chat) {
        this.chats.put(id, chat);
    }

    @Override
    public void unregisterAll() {
        chats.clear();
    }

    @Override
    public Map<String, Chat> getChats() {
        return Collections.unmodifiableMap(chats);
    }

}

package ru.brikster.chatty.chat.registry;

import ru.brikster.chatty.api.chat.Chat;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Singleton
public class MemoryChatRegistry implements ChatRegistry {

    private final List<Chat> chats = new ArrayList<>();

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
        return Collections.unmodifiableList(chats);
    }

}

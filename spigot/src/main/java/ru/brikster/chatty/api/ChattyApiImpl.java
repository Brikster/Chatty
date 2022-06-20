package ru.brikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Collection;

public class ChattyApiImpl implements ChattyApi {

    private final Collection<Chat> chats;

    public ChattyApiImpl(Collection<Chat> chats) {
        this.chats = chats;
    }

    @Override
    @NotNull
    public Collection<Chat> getChats() {
        return chats;
    }

}

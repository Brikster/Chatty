package ru.mrbrikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.chatty.api.chats.Chat;

import java.util.Collection;
import java.util.Optional;

public class ChattyApiImplementation implements ChattyApi {

    private final Collection<Chat> chats;

    public ChattyApiImplementation(Collection<Chat> chats) {
        this.chats = chats;
    }

    @Override
    @NotNull
    public Collection<Chat> getChats() {
        return chats;
    }

    @Override
    @NotNull
    public Optional<Chat> getChat(String name) {
        return getChats().stream().filter(chat -> chat.getName().equalsIgnoreCase(name)).findAny();
    }

}

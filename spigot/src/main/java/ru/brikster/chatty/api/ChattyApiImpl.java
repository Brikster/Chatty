package ru.brikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Map;

public final class ChattyApiImpl implements ChattyApi {

    private final Map<String, Chat> chats;

    public ChattyApiImpl(Map<String, Chat> chats) {
        this.chats = chats;
    }

    @Override
    @NotNull
    public Map<String, Chat> getChats() {
        return chats;
    }

    public static void updateInstance(ChattyApi chattyApi) {
        ChattyApiInstanceContainer.API_INSTANCE = chattyApi;
    }

}

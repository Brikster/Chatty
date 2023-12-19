package ru.brikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Map;

final class ChattyApiInstanceContainer {

    public static ChattyApi API_INSTANCE;

    public static final ChattyApi PROXYING_API_INSTANCE = new ChattyApi() {

        @Override
        public @NotNull Map<String, Chat> getChats() {
            return API_INSTANCE.getChats();
        }
    };

}

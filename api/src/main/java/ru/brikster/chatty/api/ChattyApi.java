package ru.brikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Map;
import java.util.Objects;

public interface ChattyApi {

    static @NotNull ChattyApi instance() {
        return Objects.requireNonNull(ChattyApiInstanceContainer.PROXYING_API_INSTANCE, "Chatty is not initialized yet");
    }

    /**
     * Map of currently registered chats
     *
     * @return chats map
     */
    @NotNull
    Map<String, Chat> getChats();

}

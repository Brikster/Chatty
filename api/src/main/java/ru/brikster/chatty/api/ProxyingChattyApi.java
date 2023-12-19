package ru.brikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Map;

/**
 * Created as a singleton for the main ChattyApiImpl instance, that
 * is re-instantiated on every reload
 */
final class ProxyingChattyApi implements ChattyApi {

    @Override
    public @NotNull Map<String, Chat> getChats() {
        return ChattyApiInstanceContainer.API_INSTANCE.getChats();
    }

}

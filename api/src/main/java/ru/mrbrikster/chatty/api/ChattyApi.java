package ru.mrbrikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.chatty.api.chats.Chat;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Optional;

public interface ChattyApi {

    static ChattyApi get() {
        return ChattyApiHolder.getApi();
    }

    /**
     * Returns collection of enabled chats
     *
     * @return collection of enabled chats
     */
    @NotNull
    Collection<Chat> getChats();

    /**
     * Returns chat with given name, if exists
     *
     * @param name chat name
     * @return optional chat with given name
     */
    @NotNull
    Optional<Chat> getChat(String name);

    class ChattyApiHolder {

        private static @Getter @Setter ChattyApi api;

    }

}

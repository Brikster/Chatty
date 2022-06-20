package ru.brikster.chatty.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

public interface ChattyApi {

    static ChattyApi get() {
        return ChattyApiHolder.getApi();
    }

    /**
     * Returns collection of chats
     *
     * @return collection of chats
     */
    @NotNull
    Collection<Chat> getChats();

    /**
     * Returns chat with given name, if exists
     *
     * @param name chat name
     * @return chat with given name
     */
    @Nullable
    default Chat getChat(String name) {
        return getChats()
                .stream()
                .filter(chat -> chat.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    class ChattyApiHolder {

        private static @Getter @Setter ChattyApi api;

    }

}

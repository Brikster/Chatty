package ru.mrbrikster.chatty.chat.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.chatty.chat.Chat;

import lombok.Getter;

import java.util.HashSet;

public class ChattyAsyncPlayerChatEvent extends AsyncPlayerChatEvent {

    private @Getter final Chat chat;

    /**.
     * @param who     the chat sender
     * @param message the message sent
     * @param chat    the chat to send message
     */
    public ChattyAsyncPlayerChatEvent(@NotNull Player who, @NotNull String message, Chat chat) {
        super(true, who, message, new HashSet<>(Bukkit.getOnlinePlayers()));

        this.chat = chat;
    }

}

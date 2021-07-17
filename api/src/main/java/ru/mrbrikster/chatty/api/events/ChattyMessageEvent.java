package ru.mrbrikster.chatty.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.chatty.api.chats.Chat;

public class ChattyMessageEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Chat chat;
    private final String message;

    public ChattyMessageEvent(@NotNull Player player, @NotNull Chat chat, @NotNull String message) {
        super(true);
        this.player = player;
        this.chat = chat;
        this.message = message;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Returns the player that sends a message
     *
     * @return player that sends a message
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the chat to which message sends
     *
     * @return chat to which message sends
     */
    @NotNull
    public Chat getChat() {
        return chat;
    }

    /**
     * Returns the message typed by player
     *
     * @return message typed by player
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}

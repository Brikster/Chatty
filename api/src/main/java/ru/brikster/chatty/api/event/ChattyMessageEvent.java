package ru.brikster.chatty.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.List;

public final class ChattyMessageEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Chat chat;
    private final String plainMessage;
    private final List<Player> recipients;

    public ChattyMessageEvent(@NotNull Player player,
                              @NotNull Chat chat,
                              @NotNull String plainMessage,
                              @NotNull List<Player> recipients) {
        super(true);
        this.player = player;
        this.chat = chat;
        this.plainMessage = plainMessage;
        this.recipients = recipients;
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
    public Player getSender() {
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
     * Returns the plain message text, typed by player
     *
     * @return plain message text
     */
    @NotNull
    public String getPlainMessage() {
        return plainMessage;
    }

    @NotNull
    public List<Player> getRecipients() {
        return recipients;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}

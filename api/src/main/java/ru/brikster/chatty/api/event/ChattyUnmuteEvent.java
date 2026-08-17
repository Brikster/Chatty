package ru.brikster.chatty.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called before a player's mute is lifted, off the main thread.
 *
 * <p>It is only called for a player who is actually muted. Cancelling it leaves
 * the mute in place and says nothing to whoever asked.
 */
public final class ChattyUnmuteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CommandSender source;
    private final UUID targetUniqueId;
    private final String targetName;

    private boolean cancelled;

    public ChattyUnmuteEvent(@NotNull CommandSender source,
                             @NotNull UUID targetUniqueId,
                             @NotNull String targetName) {
        super(true);
        this.source = source;
        this.targetUniqueId = targetUniqueId;
        this.targetName = targetName;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Returns who asked for the unmute — a player, or the console.
     *
     * @return the sender of the command
     */
    public @NotNull CommandSender getSource() {
        return source;
    }

    /**
     * Returns the id of the player being unmuted, who may well be offline.
     *
     * @return the unmuted player's unique id
     */
    public @NotNull UUID getTargetUniqueId() {
        return targetUniqueId;
    }

    /**
     * Returns the name the unmute was issued against.
     *
     * @return the unmuted player's name
     */
    public @NotNull String getTargetName() {
        return targetName;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}

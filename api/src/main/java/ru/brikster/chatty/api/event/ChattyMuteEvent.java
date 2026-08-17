package ru.brikster.chatty.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called before a player is muted, off the main thread.
 *
 * <p>Cancelling it leaves the player unmuted and says nothing to whoever asked;
 * a plugin that vetoes a mute is expected to explain itself. The duration and
 * the reason may also be changed, and the mute is then stored as changed.
 */
public final class ChattyMuteEvent extends Event implements Cancellable {

    /** The value {@link #getUntil()} carries for a mute that never expires. */
    public static final long PERMANENT = Long.MAX_VALUE;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CommandSender source;
    private final UUID targetUniqueId;
    private final String targetName;

    private long until;
    private @Nullable String reason;
    private boolean cancelled;

    public ChattyMuteEvent(@NotNull CommandSender source,
                           @NotNull UUID targetUniqueId,
                           @NotNull String targetName,
                           long until,
                           @Nullable String reason) {
        super(true);
        this.source = source;
        this.targetUniqueId = targetUniqueId;
        this.targetName = targetName;
        this.until = until;
        this.reason = reason;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Returns who asked for the mute — a player, or the console.
     *
     * @return the sender of the command
     */
    public @NotNull CommandSender getSource() {
        return source;
    }

    /**
     * Returns the id of the player being muted, who may well be offline.
     *
     * @return the muted player's unique id
     */
    public @NotNull UUID getTargetUniqueId() {
        return targetUniqueId;
    }

    /**
     * Returns the name the mute was issued against.
     *
     * @return the muted player's name
     */
    public @NotNull String getTargetName() {
        return targetName;
    }

    /**
     * Returns when the mute runs out, as epoch milliseconds, or
     * {@link #PERMANENT}.
     *
     * @return the moment the mute expires
     */
    public long getUntil() {
        return until;
    }

    public void setUntil(long until) {
        this.until = until;
    }

    /**
     * Returns whether the mute never expires.
     *
     * @return true when the mute is permanent
     */
    public boolean isPermanent() {
        return until == PERMANENT;
    }

    /**
     * Returns the reason given for the mute, if one was.
     *
     * @return the reason, or null
     */
    public @Nullable String getReason() {
        return reason;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
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

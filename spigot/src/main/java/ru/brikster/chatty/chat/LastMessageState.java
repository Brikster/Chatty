package ru.brikster.chatty.chat;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What each player said last, so a command run afterwards can refer to it.
 * A moderator's jail command or a Discord bridge needs the message and who it
 * was aimed at, and neither is available from a plain PlaceholderAPI request.
 */
@Singleton
public final class LastMessageState {

    @Value
    public static class LastMessage {

        @NotNull String message;
        @NotNull String targetName;

    }

    private final Map<UUID, LastMessage> messages = new ConcurrentHashMap<>();

    public void remember(@NotNull UUID playerUuid, @NotNull String message, @NotNull String targetName) {
        messages.put(playerUuid, new LastMessage(message, targetName));
    }

    public @Nullable LastMessage get(@NotNull UUID playerUuid) {
        return messages.get(playerUuid);
    }

    public void forget(@NotNull UUID playerUuid) {
        messages.remove(playerUuid);
    }

}

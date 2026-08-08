package ru.brikster.chatty.repository.player;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class Mute {

    public static final long PERMANENT = Long.MAX_VALUE;

    long until;
    @Nullable String reason;

    public boolean isPermanent() {
        return until == PERMANENT;
    }

    public boolean isExpired(long now) {
        return !isPermanent() && until <= now;
    }

    public static @NotNull Mute permanent(@Nullable String reason) {
        return new Mute(PERMANENT, reason);
    }

}

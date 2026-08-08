package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.Mute;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class MuteFormatter {

    private static final Pattern DURATION = Pattern.compile("(?i)^(\\d+)([smhdw])$");

    public @Nullable Long parseDuration(String input) {
        Matcher matcher = DURATION.matcher(input.trim());
        if (!matcher.matches()) {
            return null;
        }
        long amount = Long.parseLong(matcher.group(1));
        switch (matcher.group(2).toLowerCase()) {
            case "s": return TimeUnit.SECONDS.toMillis(amount);
            case "m": return TimeUnit.MINUTES.toMillis(amount);
            case "h": return TimeUnit.HOURS.toMillis(amount);
            case "d": return TimeUnit.DAYS.toMillis(amount);
            case "w": return TimeUnit.DAYS.toMillis(amount * 7);
            default: return null;
        }
    }

    public String describe(Mute mute, MessagesConfig messages) {
        if (mute.isPermanent()) {
            return messages.getMutePermanently();
        }
        long left = Math.max(0, mute.getUntil() - System.currentTimeMillis());
        long days = TimeUnit.MILLISECONDS.toDays(left);
        if (days > 0) {
            return messages.getMuteDurationDays()
                    .replace("{days}", Long.toString(days))
                    .replace("{hours}", Long.toString(TimeUnit.MILLISECONDS.toHours(left) % 24));
        }
        long hours = TimeUnit.MILLISECONDS.toHours(left);
        if (hours > 0) {
            return messages.getMuteDurationHours()
                    .replace("{hours}", Long.toString(hours))
                    .replace("{minutes}", Long.toString(TimeUnit.MILLISECONDS.toMinutes(left) % 60));
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(left);
        if (minutes > 0) {
            return messages.getMuteDurationMinutes()
                    .replace("{minutes}", Long.toString(minutes))
                    .replace("{seconds}", Long.toString(TimeUnit.MILLISECONDS.toSeconds(left) % 60));
        }
        return messages.getMuteDurationSeconds()
                .replace("{seconds}", Long.toString(TimeUnit.MILLISECONDS.toSeconds(left)));
    }

}

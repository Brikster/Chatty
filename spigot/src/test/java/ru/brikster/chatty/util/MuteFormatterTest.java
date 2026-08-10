package ru.brikster.chatty.util;

import org.junit.jupiter.api.Test;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.Mute;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MuteFormatterTest {

    private final MessagesConfig messages = new MessagesConfig();

    @Test
    void readsEveryUnitItOffers() {
        assertEquals(TimeUnit.SECONDS.toMillis(30), MuteFormatter.parseDuration("30s"));
        assertEquals(TimeUnit.MINUTES.toMillis(15), MuteFormatter.parseDuration("15m"));
        assertEquals(TimeUnit.HOURS.toMillis(2), MuteFormatter.parseDuration("2h"));
        assertEquals(TimeUnit.DAYS.toMillis(3), MuteFormatter.parseDuration("3d"));
        assertEquals(TimeUnit.DAYS.toMillis(14), MuteFormatter.parseDuration("2w"));
    }

    @Test
    void acceptsTheUnitInEitherCaseAndIgnoresPadding() {
        assertEquals(TimeUnit.HOURS.toMillis(1), MuteFormatter.parseDuration("1H"));
        assertEquals(TimeUnit.HOURS.toMillis(1), MuteFormatter.parseDuration("  1h "));
    }

    @Test
    void refusesAnythingItCannotReadRatherThanGuessing() {
        assertNull(MuteFormatter.parseDuration("soon"), "a bare word is not a duration");
        assertNull(MuteFormatter.parseDuration("10"), "a bare number has no unit");
        assertNull(MuteFormatter.parseDuration("10y"), "years are not offered");
        assertNull(MuteFormatter.parseDuration("1h30m"), "only one unit is supported");
        assertNull(MuteFormatter.parseDuration(""));
    }

    @Test
    void aPermanentMuteReadsFromTheLangFile() {
        assertEquals(messages.getMutePermanently(), MuteFormatter.describe(Mute.permanent(null), messages));
    }

    @Test
    void everyUnitPairComesFromTheLangFile() {
        assertEquals("2d 1h", describeIn(TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(1)));
        assertEquals("3h 4m", describeIn(TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(4)));
        assertEquals("5m 6s", describeIn(TimeUnit.MINUTES.toMillis(5) + TimeUnit.SECONDS.toMillis(6)));
        assertEquals("7s", describeIn(TimeUnit.SECONDS.toMillis(7)));
    }

    @Test
    void everyPlaceholderIsFilledIn() {
        String description = describeIn(TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(2));

        assertEquals("1d 2h", description);
        org.junit.jupiter.api.Assertions.assertFalse(description.contains("{"),
                "an unreplaced placeholder would reach the player as text");
    }

    @Test
    void anExpiredMuteCountsDownToZeroInsteadOfGoingNegative() {
        Mute expired = new Mute(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5), null);
        assertEquals("0s", MuteFormatter.describe(expired, messages));
    }

    @Test
    void aTranslatedLangFileChangesTheWording() {
        MessagesConfig russian = new MessagesConfig();
        setField(russian, "mutePermanently", "навсегда");
        setField(russian, "muteDurationDays", "{days}д {hours}ч");

        assertEquals("навсегда", MuteFormatter.describe(Mute.permanent(null), russian));
        assertEquals("2д 1ч", MuteFormatter.describe(
                new Mute(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)
                        + TimeUnit.HOURS.toMillis(1) + 500, null), russian));
    }

    private String describeIn(long millisFromNow) {
        return MuteFormatter.describe(
                new Mute(System.currentTimeMillis() + millisFromNow + 500, null), messages);
    }

    private static void setField(MessagesConfig config, String name, String value) {
        try {
            java.lang.reflect.Field field = MessagesConfig.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(config, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

}

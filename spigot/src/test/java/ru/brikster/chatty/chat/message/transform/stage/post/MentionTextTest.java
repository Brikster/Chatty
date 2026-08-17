package ru.brikster.chatty.chat.message.transform.stage.post;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A mention used to carry a zero-width space behind it, meant to stop its
 * colour reaching the following text. Minecraft's font has no glyph for that
 * codepoint, so clients drew a box after every mentioned name - and with a
 * mention pattern that does not require "@", every name in a sentence is a
 * mention, so the boxes were everywhere.
 *
 * <p>These tests hold the reasons it was safe to drop: nothing bleeds without it.
 */
class MentionTextTest {

    private static final Pattern ALEX = Pattern.compile("(?i)@" + Pattern.quote("Alex"));

    @Test
    void aMentionAddsNothingUnprintableToTheMessage() {
        String plain = PlainTextComponentSerializer.plainText().serialize(mention(coloured()));

        assertEquals("hello @Alex world", plain);
        assertFalse(plain.chars().anyMatch(c -> Character.getType(c) == Character.FORMAT),
                "an invisible formatting codepoint is what clients drew as a box");
    }

    @Test
    void theTextAfterAMentionCarriesNoColourOfItsOwn() {
        Component result = mention(coloured());

        Component trailing = result.children().get(result.children().size() - 1);
        assertEquals(" world", PlainTextComponentSerializer.plainText().serialize(trailing));
        assertNull(trailing.color(), "the mention's colour must not become the tail's colour");
    }

    @Test
    void legacyOutputResetsAfterAMentionByItself() {
        String legacy = LegacyComponentSerializer.legacySection().serialize(mention(coloured()));

        assertEquals("hello §a@Alex§r world", legacy,
                "the serializer already resets, which is why no marker is needed");
    }

    @Test
    void aDecoratedMentionDoesNotLeaveTheRestBold() {
        Component bold = Component.text("@Alex", NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);
        String legacy = LegacyComponentSerializer.legacySection().serialize(mention(bold));

        assertEquals("hello §e§l@Alex§r world", legacy);
    }

    private static Component coloured() {
        return Component.text("@Alex", NamedTextColor.GREEN);
    }

    private static Component mention(Component replacement) {
        return Component.text("hello @Alex world").replaceText(TextReplacementConfig.builder()
                .match(ALEX)
                .replacement(replacement)
                .build());
    }

}

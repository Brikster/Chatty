package ru.brikster.chatty.notification.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancementJsonTest {

    private static final Component TITLE = Component.text("First line");
    private static final Component SUBTITLE = Component.text("Second line");

    @Test
    void aToastWithoutASubtitleKeepsASingleLineTitle() {
        Component heading = AdvancementJson.heading(TITLE, Component.empty());

        assertEquals("First line", plain(heading));
        assertFalse(plain(heading).contains("\n"),
                "a single-line title lets the client print its own wording for the frame");
    }

    @Test
    void aSubtitleIsFoldedIntoTheTitleBehindANewline() {
        Component heading = AdvancementJson.heading(TITLE, SUBTITLE);

        assertEquals("First line\nSecond line", plain(heading),
                "the client only drops its own frame wording when the title spans two lines");
    }

    @Test
    void aBlankSubtitleCountsAsAbsent() {
        assertEquals("First line", plain(AdvancementJson.heading(TITLE, Component.text(""))));
    }

    @Test
    void theToastAsksForAPopUpAndNothingElse() {
        JsonObject display = displayOf(AdvancementJson.toast("chatty:notification/root",
                TITLE, "minecraft:diamond", AdvancementFrame.TASK, true));

        assertTrue(display.get("show_toast").getAsBoolean());
        assertFalse(display.get("announce_to_chat").getAsBoolean(),
                "a toast must not also post the advancement into chat");
        assertTrue(display.get("hidden").getAsBoolean());
    }

    @Test
    void theToastHangsOffTheGivenParent() {
        JsonObject toast = parse(AdvancementJson.toast("chatty:notification/root",
                TITLE, "minecraft:diamond", AdvancementFrame.TASK, true));

        assertEquals("chatty:notification/root", toast.get("parent").getAsString(),
                "without a parent the toast is a root and gets its own advancement tab");
    }

    @Test
    void theRootCarriesNoDisplay() {
        JsonObject root = parse(AdvancementJson.root());

        assertFalse(root.has("display"),
                "a root with a display gets a tab in the advancement screen");
        assertTrue(root.has("criteria"));
        assertFalse(root.has("parent"));
    }

    @Test
    void theIconFieldFollowsTheServerGeneration() {
        JsonObject modern = displayOf(AdvancementJson.toast("p", TITLE, "minecraft:diamond",
                AdvancementFrame.TASK, true));
        JsonObject legacy = displayOf(AdvancementJson.toast("p", TITLE, "minecraft:diamond",
                AdvancementFrame.TASK, false));

        assertEquals("minecraft:diamond", modern.getAsJsonObject("icon").get("id").getAsString(),
                "1.20.5 and newer name the item \"id\"");
        assertEquals("minecraft:diamond", legacy.getAsJsonObject("icon").get("item").getAsString(),
                "older servers name it \"item\"");
    }

    @Test
    void theFrameIsWrittenAsTheGameSpellsIt() {
        for (AdvancementFrame frame : AdvancementFrame.values()) {
            JsonObject display = displayOf(AdvancementJson.toast("p", TITLE,
                    "minecraft:diamond", frame, true));
            assertEquals(frame.name().toLowerCase(), display.get("frame").getAsString());
        }
    }

    @Test
    void everyCriterionIsRequired() {
        JsonObject toast = parse(AdvancementJson.toast("p", TITLE, "minecraft:diamond",
                AdvancementFrame.TASK, true));

        assertTrue(toast.getAsJsonObject("criteria").has(AdvancementJson.CRITERION));
        assertEquals(AdvancementJson.CRITERION, toast.getAsJsonArray("requirements")
                .get(0).getAsJsonArray().get(0).getAsString());
    }

    @Test
    void theFingerprintIsStableForTheSameLook() {
        assertEquals(
                AdvancementJson.fingerprint(TITLE, "minecraft:diamond", AdvancementFrame.TASK),
                AdvancementJson.fingerprint(Component.text("First line"), "minecraft:diamond",
                        AdvancementFrame.TASK),
                "an unchanged toast must keep its key, or every reload registers it again");
    }

    @Test
    void theFingerprintFollowsEveryVisibleDetail() {
        String base = AdvancementJson.fingerprint(TITLE, "minecraft:diamond", AdvancementFrame.TASK);

        assertNotEquals(base, AdvancementJson.fingerprint(Component.text("Other"),
                "minecraft:diamond", AdvancementFrame.TASK), "title");
        assertNotEquals(base, AdvancementJson.fingerprint(TITLE, "minecraft:stone",
                AdvancementFrame.TASK), "icon");
        assertNotEquals(base, AdvancementJson.fingerprint(TITLE, "minecraft:diamond",
                AdvancementFrame.GOAL), "frame");
        assertNotEquals(base, AdvancementJson.fingerprint(
                AdvancementJson.heading(TITLE, SUBTITLE), "minecraft:diamond", AdvancementFrame.TASK),
                "subtitle");
    }

    @Test
    void theFingerprintSeesColour() {
        assertNotEquals(
                AdvancementJson.fingerprint(Component.text("x"), "i", AdvancementFrame.TASK),
                AdvancementJson.fingerprint(
                        Component.text("x").color(net.kyori.adventure.text.format.NamedTextColor.RED),
                        "i", AdvancementFrame.TASK),
                "recolouring a toast must give it a new key too");
    }

    @Test
    void aChannelIdBecomesAUsableKey() {
        assertEquals("my_channel", AdvancementJson.sanitize("My Channel"));
        assertEquals("a-b.c_d", AdvancementJson.sanitize("a-b.c_d"));
        assertEquals("____", AdvancementJson.sanitize("!@#$"));
        assertEquals("____", AdvancementJson.sanitize("Ключ"),
                "a NamespacedKey takes no non-latin characters");
    }

    private static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static JsonObject displayOf(String json) {
        return parse(json).getAsJsonObject("display");
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}

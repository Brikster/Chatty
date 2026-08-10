package ru.brikster.chatty.convert;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.brikster.chatty.convert.component.InternalMiniMessageStringConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins down which formats a server owner may write in a Chatty config, so the
 * answer to "is this syntax supported?" is checkable rather than remembered.
 */
class SupportedFormatsTest {

    private final InternalMiniMessageStringConverter converter = new InternalMiniMessageStringConverter();

    @ParameterizedTest
    @ValueSource(strings = {
            "&#FF0000red",              // Paper hex
            "&x&f&f&0&0&0&0red",        // Spigot hex
            "{#FF0000}red",             // Chatty hex
            "<#FF0000>red",             // MiniMessage hex
            "<color:#FF0000>red</color>",
    })
    void everyHexSpellingReachesTheSameColour(String input) {
        assertEquals(TextColor.fromHexString("#FF0000"), firstColour(convert(input)),
                input + " did not produce red");
    }

    @Test
    void legacyCodesStillWork() {
        assertEquals(NamedTextColor.RED, firstColour(convert("&cred")));
        assertTrue(json(convert("&lbold")).contains("\"bold\":true"));
        assertEquals("plain", plain(convert("&r&fplain")).trim());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<gradient:#FF0000:#0000FF>two stops</gradient>",
            "<gradient:#FF0000:#00FF00:#0000FF>three stops</gradient>",
            "{#FF0000:#0000FF chatty gradient}",
            "<rainbow>rainbow</rainbow>",
    })
    void gradientsSpreadColourAcrossTheText(String input) {
        Component component = convert(input);
        assertTrue(json(component).split("\"color\"", -1).length > 3,
                input + " produced no per-character colours");
    }

    @Test
    void theShadowTagIsSupported() {
        assertTrue(json(convert("<shadow:yellow>hi</shadow>")).contains("shadow_color"),
                "<shadow> is the 1.21.4+ text shadow, not <shadow_color>");
        assertTrue(json(convert("<shadow:aqua:0.5>hi</shadow>")).contains("shadow_color"),
                "the alpha form must parse too");
    }

    @Test
    void theKeybindTagIsSupported() {
        assertTrue(json(convert("Press <key:key.jump>")).contains("\"keybind\":\"key.jump\""));
    }

    @Test
    void theInsertionTagIsSupported() {
        assertTrue(json(convert("<insert:test>this</insert>")).contains("\"insertion\":\"test\""));
    }

    @Test
    void theSpriteTagIsSupported() {
        // The plain-text form of a sprite is a placeholder, so assert the node.
        assertTrue(json(convert("<sprite:icon/heart>")).contains("\"sprite\""),
                "<sprite> is what draws an atlas icon inline");
    }

    @Test
    void translatableAndInteractiveTagsAreSupported() {
        assertTrue(json(convert("<lang:block.minecraft.stone>")).contains("\"translate\""));
        assertTrue(json(convert("<hover:show_text:'hi'>x</hover>")).contains("hover_event"));
        assertTrue(json(convert("<click:suggest_command:/help>x</click>")).contains("click_event"));
        assertTrue(json(convert("<transition:#FF0000:#0000FF:0.5>x</transition>")).contains("\"color\""));
    }

    @Test
    void theCmiGradientSpellingIsNotSupported() {
        String input = "{#FF0000>}text{#0000FF<}";
        assertEquals(input, plain(convert(input)),
                "CMI writes gradients as {#hex>}text{#hex<}; Chatty passes it through"
                        + " untouched, so change this test deliberately if that changes");
    }

    private Component convert(String input) {
        Component component = converter.stringToComponent(input);
        assertNotNull(component);
        return component;
    }

    private static TextColor firstColour(Component component) {
        if (component.color() != null) {
            return component.color();
        }
        for (Component child : component.children()) {
            TextColor colour = firstColour(child);
            if (colour != null) {
                return colour;
            }
        }
        return null;
    }

    private static String json(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}

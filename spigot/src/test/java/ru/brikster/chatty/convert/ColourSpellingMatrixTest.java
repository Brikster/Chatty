package ru.brikster.chatty.convert;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.convert.component.InternalMiniMessageStringConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ColourSpellingMatrixTest {

    private final InternalMiniMessageStringConverter converter = new InternalMiniMessageStringConverter();

    @Test
    void legacyAmpersandColourIsSupported() {
        assertRendersAs("&cred text", "red text", NamedTextColor.RED);
    }

    @Test
    void sectionSignColourIsSupported() {
        assertRendersAs("§cred text", "red text", NamedTextColor.RED);
    }

    @Test
    void miniMessageNamedColourIsSupported() {
        assertRendersAs("<red>red text", "red text", NamedTextColor.RED);
    }

    @Test
    void miniMessageHexIsSupported() {
        assertRendersAs("<#757575>grey text", "grey text", TextColor.fromHexString("#757575"));
    }

    @Test
    void ampersandHexIsSupported() {
        assertRendersAs("&#757575grey text", "grey text", TextColor.fromHexString("#757575"));
    }

    @Test
    void spigotSpreadHexIsSupported() {
        assertRendersAs("&x&7&5&7&5&7&5grey text", "grey text", TextColor.fromHexString("#757575"));
    }

    @Test
    void bareHashHexIsNotAColourCode() {
        assertRendersAs("#757575grey text", "#757575grey text", null);
    }

    @Test
    void gradientIsSupported() {
        Component component = converter.stringToComponent("<gradient:#ff0000:#00ff00>fade</gradient>");
        assertEquals("fade", plain(component));
        assertNotNull(colours(component).get(0), "a gradient must colour its text");
    }

    @Test
    void rainbowIsSupported() {
        Component component = converter.stringToComponent("<rainbow>bright</rainbow>");
        assertEquals("bright", plain(component));
        assertNotNull(colours(component).get(0), "a rainbow must colour its text");
    }

    @Test
    void legacyDecorationIsSupported() {
        Component component = converter.stringToComponent("&lbold text");
        assertEquals("bold text", plain(component));
    }

    private void assertRendersAs(String spelling, String expectedText, TextColor expectedColour) {
        Component component = converter.stringToComponent(spelling);
        assertEquals(expectedText, plain(component),
                "wrong text for spelling " + spelling);
        TextColor actual = colours(component).get(0);
        if (expectedColour == null) {
            assertNull(actual, "spelling " + spelling + " must not produce a colour");
        } else {
            assertEquals(expectedColour, actual, "wrong colour for spelling " + spelling);
        }
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static List<TextColor> colours(Component component) {
        List<TextColor> colours = new ArrayList<>();
        ComponentFlattener.textOnly();
        collect(component, null, colours);
        if (colours.isEmpty()) {
            colours.add(null);
        }
        return colours;
    }

    private static void collect(Component component, TextColor inherited, List<TextColor> out) {
        TextColor own = component.color() != null ? component.color() : inherited;
        if (!plain(component).isEmpty() && component.children().isEmpty()) {
            out.add(own);
        }
        for (Component child : component.children()) {
            collect(child, own, out);
        }
    }

}

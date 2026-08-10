package ru.brikster.chatty.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.convert.component.InternalMiniMessageStringConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChattyMessagesTest {

    private final InternalMiniMessageStringConverter converter = new InternalMiniMessageStringConverter();

    private final List<String> chat = new ArrayList<>();
    private final List<String> actionBar = new ArrayList<>();

    private final Audience audience = new Audience() {
        @Override
        public void sendMessage(Component message) {
            chat.add(plain(message));
        }

        @Override
        public void sendActionBar(Component message) {
            actionBar.add(plain(message));
        }
    };

    @Test
    void anOrdinaryMessageGoesToChat() {
        ChattyMessages.send(audience, converter.stringToComponent("<red>Nobody heard you."));

        assertEquals(List.of("Nobody heard you."), chat);
        assertEquals(List.of(), actionBar);
    }

    @Test
    void thePrefixMovesTheMessageToTheActionBar() {
        ChattyMessages.send(audience, converter.stringToComponent("actionbar! <red>Nobody heard you."));

        assertEquals(List.of("Nobody heard you."), actionBar,
                "the prefix itself must not reach the player");
        assertEquals(List.of(), chat);
    }

    @Test
    void thePrefixWorksWithoutASpaceAfterIt() {
        ChattyMessages.send(audience, converter.stringToComponent("actionbar!<red>Nobody heard you."));

        assertEquals(List.of("Nobody heard you."), actionBar);
    }

    @Test
    void formattingSurvivesTheMove() {
        ChattyMessages.send(audience, converter.stringToComponent("actionbar! &cred"));

        assertEquals(List.of("red"), actionBar);
    }

    @Test
    void onlyALeadingPrefixCounts() {
        ChattyMessages.send(audience, converter.stringToComponent("say actionbar! please"));

        assertEquals(List.of("say actionbar! please"), chat,
                "the word in the middle of a sentence is just text");
        assertEquals(List.of(), actionBar);
    }

    @Test
    void aPrefixInsideAColourTagIsStillFound() {
        ChattyMessages.send(audience, converter.stringToComponent("<red>actionbar! hidden"));

        assertEquals(List.of("hidden"), actionBar,
                "the owner may colour the whole line, prefix included");
    }

    @Test
    void onlyTheFirstOccurrenceIsRemoved() {
        ChattyMessages.send(audience, converter.stringToComponent("actionbar! actionbar! twice"));

        assertEquals(List.of("actionbar! twice"), actionBar);
    }

    @Test
    void aColouredMessageKeepsItsColourOnTheActionBar() {
        List<Component> captured = new ArrayList<>();
        Audience capturing = new Audience() {
            @Override
            public void sendActionBar(Component message) {
                captured.add(message);
            }
        };

        ChattyMessages.send(capturing, converter.stringToComponent("actionbar! <red>red"));

        assertTrue(captured.size() == 1);
        assertTrue(hasColour(captured.get(0), NamedTextColor.RED), "the colour must survive");
    }

    private static boolean hasColour(Component component, NamedTextColor colour) {
        if (colour.equals(component.color())) {
            return true;
        }
        for (Component child : component.children()) {
            if (hasColour(child, colour)) {
                return true;
            }
        }
        return false;
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}

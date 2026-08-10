package ru.brikster.chatty.chat.message.context;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.context.MessageContextKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageContextKeysTest {

    private final Map<String, Object> metadata = new HashMap<>();
    private final MessageContext<?> context = context();

    @Test
    void bothKeysAreSpelledTheSameWay() {
        for (String key : List.of(MessageContextKeys.ALL_RECIPIENTS, MessageContextKeys.SPY_RECIPIENTS)) {
            assertEquals(key.toLowerCase(), key, key + " must be lower case");
            assertFalse(key.contains("_"),
                    key + " must use hyphens, like every other identifier Chatty exposes");
        }
    }

    @Test
    void whatIsPutInComesBackOut() {
        Player steve = mock(Player.class);
        Player alex = mock(Player.class);

        MessageContextKeys.putPlayers(context, MessageContextKeys.ALL_RECIPIENTS, List.of(steve, alex));

        assertEquals(List.of(steve, alex),
                MessageContextKeys.getPlayers(context, MessageContextKeys.ALL_RECIPIENTS));
        assertTrue(MessageContextKeys.has(context, MessageContextKeys.ALL_RECIPIENTS));
    }

    @Test
    void aMissingKeyReadsAsEmptyRatherThanThrowing() {
        assertFalse(MessageContextKeys.has(context, MessageContextKeys.SPY_RECIPIENTS));
        assertEquals(List.of(), MessageContextKeys.getPlayers(context, MessageContextKeys.SPY_RECIPIENTS));
    }

    @Test
    void anAddonPuttingSomethingElseUnderTheKeyCannotBreakDelivery() {
        metadata.put(MessageContextKeys.ALL_RECIPIENTS, "not a collection");
        assertEquals(List.of(), MessageContextKeys.getPlayers(context, MessageContextKeys.ALL_RECIPIENTS));

        metadata.put(MessageContextKeys.ALL_RECIPIENTS, List.of("Steve", 42));
        assertEquals(List.of(), MessageContextKeys.getPlayers(context, MessageContextKeys.ALL_RECIPIENTS),
                "the metadata map is public API, so a foreign value must be ignored, not cast");
    }

    @Test
    void nonPlayersAreFilteredOutOfAMixedCollection() {
        Player steve = mock(Player.class);
        metadata.put(MessageContextKeys.SPY_RECIPIENTS, List.of(steve, "Alex"));

        assertEquals(List.of(steve),
                MessageContextKeys.getPlayers(context, MessageContextKeys.SPY_RECIPIENTS));
    }

    @Test
    void anySetOrListIsAccepted() {
        Player steve = mock(Player.class);
        metadata.put(MessageContextKeys.SPY_RECIPIENTS, Set.of(steve));

        assertEquals(List.of(steve),
                MessageContextKeys.getPlayers(context, MessageContextKeys.SPY_RECIPIENTS));
    }

    private MessageContext<?> context() {
        MessageContext<?> mocked = mock(MessageContext.class);
        when(mocked.getMetadata()).thenReturn(metadata);
        return mocked;
    }

}

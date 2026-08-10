package ru.brikster.chatty.chat;

import org.junit.jupiter.api.Test;
import ru.brikster.chatty.chat.LastMessageState.LastMessage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LastMessageStateTest {

    private final LastMessageState state = new LastMessageState();
    private final UUID steve = UUID.randomUUID();

    @Test
    void nothingIsRememberedBeforeTheFirstMessage() {
        assertNull(state.get(steve),
                "the expansion answers with the player's own name in this case");
    }

    @Test
    void theLastMessageReplacesTheOneBefore() {
        state.remember(steve, "first", "Steve");
        state.remember(steve, "second", "Alex");

        LastMessage last = state.get(steve);
        assertEquals("second", last.getMessage());
        assertEquals("Alex", last.getTargetName());
    }

    @Test
    void playersDoNotSeeEachOthersMessages() {
        UUID alex = UUID.randomUUID();
        state.remember(steve, "from steve", "Steve");
        state.remember(alex, "from alex", "Alex");

        assertEquals("from steve", state.get(steve).getMessage());
        assertEquals("from alex", state.get(alex).getMessage());
    }

    @Test
    void quittingClearsTheEntry() {
        state.remember(steve, "bye", "Steve");
        state.forget(steve);

        assertNull(state.get(steve), "a quitting player must not be kept in memory");
    }

}

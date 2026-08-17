package ru.brikster.chatty.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.brikster.chatty.repository.player.Mute;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ChattyEventsTest {

    @ParameterizedTest
    @ValueSource(classes = {
            ChattyInitEvent.class, ChattyMessageEvent.class, ChattyPreMessageEvent.class,
            ChattyMuteEvent.class, ChattyUnmuteEvent.class,
    })
    void everyEventCanBeRegisteredByBukkit(Class<? extends Event> event) throws Exception {
        Method handlerList = event.getMethod("getHandlerList");

        assertTrue(Modifier.isStatic(handlerList.getModifiers()),
                event.getSimpleName() + " needs a static getHandlerList, or Bukkit"
                        + " refuses to register a listener for it");
        assertEquals(HandlerList.class, handlerList.getReturnType());
        assertTrue(handlerList.invoke(null) instanceof HandlerList);
    }

    @Test
    void theEventAgreesWithStorageOnWhatPermanentMeans() {
        assertEquals(Mute.PERMANENT, ChattyMuteEvent.PERMANENT,
                "the API constant and the stored value must be the same number,"
                        + " or isPermanent() disagrees with the database");
    }

    @Test
    void aMuteEventReportsWhatItWasGiven() {
        UUID target = UUID.randomUUID();
        ChattyMuteEvent event = new ChattyMuteEvent(mock(CommandSender.class),
                target, "Steve", 1_000L, "spam");

        assertEquals(target, event.getTargetUniqueId());
        assertEquals("Steve", event.getTargetName());
        assertEquals(1_000L, event.getUntil());
        assertEquals("spam", event.getReason());
        assertFalse(event.isPermanent());
        assertTrue(event.isAsynchronous(), "the mute command runs off the main thread");
    }

    @Test
    void aPermanentMuteSaysSo() {
        ChattyMuteEvent event = new ChattyMuteEvent(mock(CommandSender.class),
                UUID.randomUUID(), "Steve", ChattyMuteEvent.PERMANENT, null);

        assertTrue(event.isPermanent());
        assertNull(event.getReason());
    }

    @Test
    void aListenerCanChangeTheDurationAndReason() {
        ChattyMuteEvent event = new ChattyMuteEvent(mock(CommandSender.class),
                UUID.randomUUID(), "Steve", 1_000L, "spam");

        event.setUntil(ChattyMuteEvent.PERMANENT);
        event.setReason("repeated spam");

        assertTrue(event.isPermanent(), "a listener may harden a mute");
        assertEquals("repeated spam", event.getReason());
    }

    @Test
    void bothMuteEventsCanBeVetoed() {
        for (Cancellable event : new Cancellable[]{
                new ChattyMuteEvent(mock(CommandSender.class), UUID.randomUUID(), "Steve", 1_000L, null),
                new ChattyUnmuteEvent(mock(CommandSender.class), UUID.randomUUID(), "Steve"),
        }) {
            assertFalse(event.isCancelled(), "an event must start uncancelled");
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }
    }

}

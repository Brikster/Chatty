package ru.brikster.chatty.chat.selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.registry.MemoryChatRegistry;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatSelectorImplTest {

    private ChatRegistry registry;
    private ChatSelectorImpl selector;

    @BeforeEach
    void setUp() throws Exception {
        registry = new MemoryChatRegistry();
        selector = new ChatSelectorImpl();
        Field field = ChatSelectorImpl.class.getDeclaredField("registry");
        field.setAccessible(true);
        field.set(selector, registry);
    }

    @Test
    void picksTheLongestMatchingSymbol() {
        registry.register("global", chat("global", "!"));
        registry.register("staff", chat("staff", "!!"));
        registry.register("local", chat("local", ""));

        assertEquals("staff", selector.selectChat("!!I found a duper", c -> true).getId());
        assertEquals("global", selector.selectChat("!hello everyone", c -> true).getId());
        assertEquals("local", selector.selectChat("hello neighbours", c -> true).getId());
    }

    @Test
    void picksTheLongestMatchingSymbolWhateverTheRegistrationOrder() {
        registry.register("staff", chat("staff", "!!"));
        registry.register("global", chat("global", "!"));

        assertEquals("staff", selector.selectChat("!!I found a duper", c -> true).getId());
    }

    @Test
    void skipsChatsTheSenderMayNotUse() {
        registry.register("global", chat("global", "!"));
        registry.register("staff", chat("staff", "!!"));

        assertEquals("global", selector.selectChat("!!I found a duper",
                c -> !c.getId().equals("staff")).getId());
    }

    @Test
    void returnsNothingWhenNoChatMatches() {
        registry.register("global", chat("global", "!"));

        assertNull(selector.selectChat("hello", c -> true));
    }

    private static Chat chat(String id, String symbol) {
        Chat chat = mock(Chat.class);
        when(chat.getId()).thenReturn(id);
        when(chat.getSymbol()).thenReturn(symbol);
        return chat;
    }

}

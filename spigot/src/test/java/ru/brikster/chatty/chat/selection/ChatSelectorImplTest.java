package ru.brikster.chatty.chat.selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import org.bukkit.entity.Player;
import ru.brikster.chatty.chat.registry.MemoryChatRegistry;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatSelectorImplTest {

    private ChatRegistry registry;
    private ChatSelectorImpl selector;
    private ChatSelectionState selectionState;
    private Player sender;

    @BeforeEach
    void setUp() throws Exception {
        registry = new MemoryChatRegistry();
        selectionState = new ChatSelectionState();
        selector = new ChatSelectorImpl();
        inject("registry", registry);
        inject("selectionState", selectionState);
        sender = mock(Player.class);
        when(sender.getUniqueId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-0000000000ff"));
    }

    @Test
    void picksTheLongestMatchingSymbol() {
        registry.register("global", chat("global", "!"));
        registry.register("staff", chat("staff", "!!"));
        registry.register("local", chat("local", ""));

        assertEquals("staff", selector.selectChat(sender, "!!I found a duper", c -> true).getId());
        assertEquals("global", selector.selectChat(sender, "!hello everyone", c -> true).getId());
        assertEquals("local", selector.selectChat(sender, "hello neighbours", c -> true).getId());
    }

    @Test
    void picksTheLongestMatchingSymbolWhateverTheRegistrationOrder() {
        registry.register("staff", chat("staff", "!!"));
        registry.register("global", chat("global", "!"));

        assertEquals("staff", selector.selectChat(sender, "!!I found a duper", c -> true).getId());
    }

    @Test
    void skipsChatsTheSenderMayNotUse() {
        registry.register("global", chat("global", "!"));
        registry.register("staff", chat("staff", "!!"));

        assertEquals("global", selector.selectChat(sender, "!!I found a duper",
                c -> !c.getId().equals("staff")).getId());
    }

    @Test
    void returnsNothingWhenNoChatMatches() {
        registry.register("global", chat("global", "!"));

        assertNull(selector.selectChat(sender, "hello", c -> true));
    }

    @Test
    void sendsTheNextMessageIntoTheChatTheCommandRequested() {
        registry.register("global", chat("global", "!"));
        registry.register("local", chat("local", ""));

        selectionState.sendNext(sender.getUniqueId(), "global");
        assertEquals("global", selector.selectChat(sender, "no symbol here", c -> true).getId());

        assertEquals("local", selector.selectChat(sender, "no symbol here", c -> true).getId(),
                "a requested chat must apply to one message only");
    }

    @Test
    void usesTheSwitchedChatWhenNoSymbolIsGiven() {
        registry.register("global", chat("global", "!"));
        registry.register("local", chat("local", ""));

        selectionState.switchTo(sender.getUniqueId(), "global");
        assertEquals("global", selector.selectChat(sender, "no symbol here", c -> true).getId());
    }

    @Test
    void letsAnExplicitSymbolOverrideTheSwitchedChat() {
        registry.register("global", chat("global", "!"));
        registry.register("staff", chat("staff", "!!"));
        registry.register("local", chat("local", ""));

        selectionState.switchTo(sender.getUniqueId(), "global");
        assertEquals("staff", selector.selectChat(sender, "!!report", c -> true).getId());
    }

    @Test
    void ignoresASwitchedChatTheSenderMayNoLongerUse() {
        registry.register("staff", chat("staff", ""));
        registry.register("local", chat("local", ""));

        selectionState.switchTo(sender.getUniqueId(), "staff");
        assertEquals("local", selector.selectChat(sender, "hello",
                c -> !c.getId().equals("staff")).getId());
    }

    private void inject(String field, Object value) throws Exception {
        Field declared = ChatSelectorImpl.class.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(selector, value);
    }

    private static Chat chat(String id, String symbol) {
        Chat chat = mock(Chat.class);
        when(chat.getId()).thenReturn(id);
        when(chat.getSymbol()).thenReturn(symbol);
        return chat;
    }

}

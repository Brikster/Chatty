package ru.brikster.chatty.chat;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.api.chat.ChatStyle;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SenderFormatTest {

    private static final ChatStyle CHAT_RED_VIEW =
            new ChatStyle("red", Component.text("chat-red"), "{original-message}", 10);
    private static final ChatStyle VIP_RED_VIEW =
            new ChatStyle("red", Component.text("vip-red"), "{original-message}", 10);

    private final SenderFormat vip = new SenderFormat("vip", 10,
            Component.text("vip"), "{vip-message}", Set.of(VIP_RED_VIEW));
    private final SenderFormat staff = new SenderFormat("staff", 20,
            Component.text("staff"), "{staff-message}", Set.of());

    @Test
    void usesTheChatFormatForASenderWithNoRank() {
        Player sender = senderWith();
        assertEquals("chat", plain(chat().getFormat(sender)));
        assertEquals("{original-message}", chat().getMessageFormat(sender));
    }

    @Test
    void usesTheRankFormatWhenTheSenderHoldsIt() {
        Player sender = senderWith("chatty.sender-format.vip");
        assertEquals("vip", plain(chat().getFormat(sender)));
        assertEquals("{vip-message}", chat().getMessageFormat(sender));
    }

    @Test
    void picksTheHighestPriorityRankTheSenderHolds() {
        Player sender = senderWith("chatty.sender-format.vip", "chatty.sender-format.staff");
        assertEquals("staff", plain(chat().getFormat(sender)));
    }

    @Test
    void acceptsAPerChatGrant() {
        Player sender = senderWith("chatty.chat.global.sender-format.vip");
        assertEquals("vip", plain(chat().getFormat(sender)));
    }

    @Test
    void ignoresARankGrantedForAnotherChat() {
        Player sender = senderWith("chatty.chat.local.sender-format.vip");
        assertEquals("chat", plain(chat().getFormat(sender)));
    }

    @Test
    void viewerStylesComeFromTheSelectedRank() {
        assertEquals(Set.of(CHAT_RED_VIEW), chat().getStyles(senderWith()));
        assertEquals(Set.of(VIP_RED_VIEW), chat().getStyles(senderWith("chatty.sender-format.vip")));
    }

    @Test
    void aRankWithoutItsOwnStylesDoesNotFallBackToTheChatStyles() {
        assertEquals(Set.of(), chat().getStyles(senderWith("chatty.sender-format.staff")),
                "a rank defining no styles must not inherit the chat's, or viewers would see the chat format");
    }

    private ChatImpl chat() {
        return new ChatImpl("global", "Global", mock(BukkitAudiences.class),
                Component.text("chat"), "{original-message}", "", null,
                -2, false, Set.of(CHAT_RED_VIEW), false, false, false, null, Component.empty(), 0,
                null, List.of(staff, vip), "", null);
    }

    private static Player senderWith(String... granted) {
        Set<String> nodes = Set.of(granted);
        Player sender = mock(Player.class);
        when(sender.hasPermission(anyString()))
                .thenAnswer(invocation -> nodes.contains(invocation.getArgument(0, String.class)));
        return sender;
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}

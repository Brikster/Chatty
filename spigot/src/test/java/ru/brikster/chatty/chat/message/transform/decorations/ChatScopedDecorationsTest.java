package ru.brikster.chatty.chat.message.transform.decorations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatScopedDecorationsTest {

    private final PlayerDecorationsFormatter formatter = new PlayerDecorationsFormatter();

    @Test
    void appliesAColourOnlyInTheChatItWasGrantedFor() {
        CommandSender sender = senderWith("chatty.chat.global.decoration.color");

        assertEquals(NamedTextColor.RED, colourOf(formatter
                        .formatMessageWithDecorations(sender, "&cred", "global")),
                "the grant names global, so global must colour");

        assertNull(colourOf(formatter.formatMessageWithDecorations(sender, "&cred", "local")),
                "the same grant must not colour another chat");
        assertEquals("&cred", plain(formatter.formatMessageWithDecorations(sender, "&cred", "local")));
    }

    @Test
    void aServerWideGrantStillAppliesEverywhere() {
        CommandSender sender = senderWith("chatty.decoration.color");

        assertEquals(NamedTextColor.RED, colourOf(formatter
                .formatMessageWithDecorations(sender, "&cred", "global")));
        assertEquals(NamedTextColor.RED, colourOf(formatter
                .formatMessageWithDecorations(sender, "&cred", "local")));
    }

    @Test
    void appliesASingleDecorationScopedToOneChat() {
        CommandSender sender = senderWith("chatty.chat.staff.decoration.bold");

        assertEquals("bold", plain(formatter
                .formatMessageWithDecorations(sender, "&lbold", "staff")));
        assertEquals("&lbold", plain(formatter
                .formatMessageWithDecorations(sender, "&lbold", "global")));
    }

    @Test
    void keepsWorkingForPrivateMessagesWhereThereIsNoChat() {
        CommandSender sender = senderWith("chatty.decoration.color");

        assertEquals(NamedTextColor.RED, colourOf(formatter
                .formatMessageWithDecorations(sender, "&cred")));

        CommandSender scoped = senderWith("chatty.chat.global.decoration.color");
        assertNull(colourOf(formatter.formatMessageWithDecorations(scoped, "&cred")),
                "a chat-scoped grant must not leak into private messages");
    }

    private static CommandSender senderWith(String... granted) {
        Set<String> nodes = Set.of(granted);
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString()))
                .thenAnswer(invocation -> nodes.contains(invocation.getArgument(0, String.class)));
        return sender;
    }

    private static NamedTextColor colourOf(Component component) {
        Component target = component.children().isEmpty() ? component : component.children().get(0);
        return (NamedTextColor) target.color();
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}

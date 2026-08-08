package ru.brikster.chatty.chat.construct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.chat.component.impl.ReplacementsStringTransformer;
import ru.brikster.chatty.convert.component.InternalMiniMessageStringConverter;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComponentFromContextConstructorImplTest {

    private ComponentFromContextConstructorImpl constructor;

    @BeforeEach
    void setUp() throws Exception {
        constructor = new ComponentFromContextConstructorImpl();
        ReplacementsStringTransformer transformer =
                (sender, message) -> message.replace("%server_name%", "Lobby");
        inject("componentStringConverter", new InternalMiniMessageStringConverter());
        inject("legacyToMiniMessageConverter", new LegacyToMiniMessageConverter());
        inject("replacementsStringTransformer", transformer);
    }

    @Test
    void resolvesPlaceholdersInsideMessageFormat() {
        String rendered = render("[%server_name%] {original-message}", "hello");
        assertTrue(rendered.contains("[Lobby] hello"),
                "message-format placeholders must be resolved, got: " + rendered);
    }

    @Test
    void doesNotResolvePlaceholdersTypedByThePlayer() {
        String rendered = render("{original-message}", "look at %server_name%");
        assertEquals("look at %server_name%", rendered.trim(),
                "a placeholder inside a player's own message must stay literal");
    }

    private String render(String messageFormat, String playerMessage) {
        Player sender = mock(Player.class);
        when(sender.getDisplayName()).thenReturn("Steve");

        @SuppressWarnings("unchecked")
        MessageContext<Component> context = mock(MessageContext.class);
        when(context.getSender()).thenReturn(sender);
        when(context.getMessage()).thenReturn(Component.text(playerMessage));
        when(context.getMessageFormat()).thenReturn(messageFormat);
        when(context.getFormat()).thenReturn(Component.text("{message}"));

        return PlainTextComponentSerializer.plainText().serialize(constructor.construct(context));
    }

    private void inject(String field, Object value) throws Exception {
        Field declared = ComponentFromContextConstructorImpl.class.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(constructor, value);
    }

}

package ru.brikster.chatty.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichComponentSurvivalTest {

    private static final Pattern MESSAGE = Pattern.compile("\\{message}");

    @Test
    void keepsATranslatableComponentInTheFormat() {
        Component format = MiniMessage.miniMessage()
                .deserialize("<lang:block.minecraft.stone> {message}");

        Component result = substitute(format);

        assertNotNull(find(result, TranslatableComponent.class),
                "a <lang:> component in the chat format must survive formatting");
        assertEquals("block.minecraft.stone",
                find(result, TranslatableComponent.class).key());
    }

    @Test
    void keepsAShadowColourInTheFormat() {
        Component format = MiniMessage.miniMessage()
                .deserialize("<shadow:#ff0000>tag</shadow> {message}");
        assertTrue(hasShadowColour(format),
                "fixture check: MiniMessage must produce a shadow colour for this input");

        Component result = substitute(format);

        assertTrue(hasShadowColour(result),
                "a <shadow:> style in the chat format must survive formatting");
    }

    @Test
    void keepsASpriteInTheFormat() {
        Component format = MiniMessage.miniMessage()
                .deserialize("<sprite:minecraft:icon/heart> {message}");

        Component result = substitute(format);

        assertTrue(hasNonTextLeaf(result),
                "a <sprite:> component must survive formatting as a component, not as text");
    }

    @Test
    void keepsPlainTextAndColoursIntact() {
        Component format = MiniMessage.miniMessage().deserialize("<red>[chat]</red> {message}");

        Component result = substitute(format);

        assertTrue(PlainTextComponentSerializer.plainText().serialize(result).contains("[chat]"));
        assertTrue(PlainTextComponentSerializer.plainText().serialize(result).contains("hello"));
        assertTrue(colours(result).contains(NamedTextColor.RED), "the colour must survive");
    }

    private static Component substitute(Component format) {
        return AdventureUtil.replaceWithEndingSpace(format, MESSAGE,
                matched -> Component.text("hello "),
                matched -> "hello ");
    }

    private static boolean hasNonTextLeaf(Component component) {
        if (!(component instanceof net.kyori.adventure.text.TextComponent)) {
            return true;
        }
        for (Component child : component.children()) {
            if (hasNonTextLeaf(child)) {
                return true;
            }
        }
        return false;
    }

    private static <T> T find(Component component, Class<T> type) {
        if (type.isInstance(component)) {
            return type.cast(component);
        }
        for (Component child : component.children()) {
            T found = find(child, type);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static boolean hasShadowColour(Component component) {
        ShadowColor shadow = component.style().shadowColor();
        if (shadow != null) {
            return true;
        }
        for (Component child : component.children()) {
            if (hasShadowColour(child)) {
                return true;
            }
        }
        return false;
    }

    private static List<Object> colours(Component component) {
        List<Object> found = new ArrayList<>();
        if (component.color() != null) {
            found.add(component.color());
        }
        for (Component child : component.children()) {
            found.addAll(colours(child));
        }
        return found;
    }

}

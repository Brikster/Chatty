package ru.brikster.chatty.util;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.format.TextDecoration.*;

@UtilityClass
public class AdventureUtil {

    @Value
    @Accessors(fluent = true)
    private static class ComponentPart {
        String text;
        TextColor color;
        Boolean obfuscated;
        Boolean bold;
        Boolean strikethrough;
        Boolean underlined;
        Boolean italic;
        ClickEvent clickEvent;
        HoverEvent<?> hoverEvent;
        String insertion;
        Key font;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class ComponentState {
        TextColor color;
        Boolean obfuscated;
        Boolean bold;
        Boolean strikethrough;
        Boolean underlined;
        Boolean italic;
        ClickEvent clickEvent;
        HoverEvent<?> hoverEvent;
        String insertion;
        Key font;

        public void applyForeign(ComponentPart part) {
            applyColorAndDecorations(part);
            if (part.clickEvent != null) {
                this.clickEvent = part.clickEvent;
            }
            if (part.hoverEvent != null) {
                this.hoverEvent = part.hoverEvent;
            }
            if (part.insertion != null) {
                this.insertion = part.insertion;
            }
            if (part.font != null) {
                this.font = part.font;
            }
        }

        public void apply(ComponentPart part) {
            applyColorAndDecorations(part);
            this.clickEvent = part.clickEvent;
            this.hoverEvent = part.hoverEvent;
            this.insertion = part.insertion;
            this.font = part.font;
        }

        private void applyColorAndDecorations(ComponentPart part) {
            if (part.color != null) {
                this.color = part.color;
                this.obfuscated = null;
                this.bold = null;
                this.strikethrough = null;
                this.underlined = null;
                this.italic = null;
            }
            if (part.obfuscated != null) {
                this.obfuscated = part.obfuscated;
            }
            if (part.bold != null) {
                this.bold = part.bold;
            }
            if (part.strikethrough != null) {
                this.strikethrough = part.strikethrough;
            }
            if (part.underlined != null) {
                this.underlined = part.underlined;
            }
            if (part.italic != null) {
                this.italic = part.italic;
            }
        }

        public Style toStyle() {
            Set<TextDecoration> decorations = new HashSet<>();

            if (obfuscated != null && obfuscated) decorations.add(OBFUSCATED);
            if (bold != null && bold) decorations.add(BOLD);
            if (strikethrough != null && strikethrough) decorations.add(STRIKETHROUGH);
            if (underlined != null && underlined) decorations.add(UNDERLINED);
            if (italic != null && italic) decorations.add(ITALIC);

            return Style.style(color, decorations)
                    .clickEvent(clickEvent)
                    .hoverEvent(hoverEvent)
                    .insertion(insertion)
                    .font(font);
        }
    }

    private static class ComponentPartsExtractionFlattener implements FlattenerListener {

        private final Deque<TextColor> color = new LinkedList<>();

        private final Deque<Boolean> obfuscated = new LinkedList<>();
        private final Deque<Boolean> bold = new LinkedList<>();
        private final Deque<Boolean> strikethrough = new LinkedList<>();
        private final Deque<Boolean> underlined = new LinkedList<>();
        private final Deque<Boolean> italic = new LinkedList<>();

        private final Deque<ClickEvent> clickEvent = new LinkedList<>();
        private final Deque<HoverEvent<?>> hoverEvent = new LinkedList<>();
        private final Deque<String> insertion = new LinkedList<>();
        private final Deque<Key> font = new LinkedList<>();

        private final List<ComponentPart> parts = new ArrayList<>();

        @Override
        public void component(@NotNull String text) {
            ComponentPart part = new ComponentPart(text,
                    color.peekLast(),
                    obfuscated.peekLast(),
                    bold.peekLast(),
                    strikethrough.peekLast(),
                    underlined.peekLast(),
                    italic.peekLast(),
                    clickEvent.peekLast(),
                    hoverEvent.peekLast(),
                    insertion.peekLast(),
                    font.peekLast());
            parts.add(part);
        }

        @Override
        public void pushStyle(@NotNull Style style) {
            if (style.color() != null) {
                this.color.add(style.color());
            }

            pushDecoration(obfuscated, style.decorations().get(OBFUSCATED));
            pushDecoration(bold, style.decorations().get(BOLD));
            pushDecoration(strikethrough, style.decorations().get(STRIKETHROUGH));
            pushDecoration(underlined, style.decorations().get(UNDERLINED));
            pushDecoration(italic, style.decorations().get(ITALIC));

            if (style.clickEvent() != null) {
                clickEvent.add(style.clickEvent());
            }

            if (style.hoverEvent() != null) {
                hoverEvent.add(style.hoverEvent());
            }

            if (style.insertion() != null) {
                insertion.add(style.insertion());
            }

            if (style.font() != null) {
                font.add(style.font());
            }
        }

        private void pushDecoration(Deque<Boolean> deque, State state) {
            if (state == State.TRUE) {
                deque.add(true);
            }
            if (state == State.FALSE) {
                deque.add(false);
            }
        }

        @Override
        public void popStyle(@NotNull Style style) {
            if (style.color() != null) {
                this.color.removeLast();
            }

            popDecoration(obfuscated, style.decorations().get(OBFUSCATED));
            popDecoration(bold, style.decorations().get(BOLD));
            popDecoration(strikethrough, style.decorations().get(STRIKETHROUGH));
            popDecoration(underlined, style.decorations().get(UNDERLINED));
            popDecoration(italic, style.decorations().get(ITALIC));

            if (style.clickEvent() != null) {
                clickEvent.removeLast();
            }

            if (style.hoverEvent() != null) {
                hoverEvent.removeLast();
            }

            if (style.insertion() != null) {
                insertion.removeLast();
            }

            if (style.font() != null) {
                font.removeLast();
            }
        }

        private void popDecoration(Deque<Boolean> deque, State state) {
            if (state != State.NOT_SET) {
                deque.removeLast();
            }
        }

        public List<ComponentPart> parts() {
            return parts;
        }

    }

    private List<ComponentPart> parts(Component component) {
        ComponentPartsExtractionFlattener flattener = new ComponentPartsExtractionFlattener();
        ComponentFlattener.basic().flatten(component, flattener);
        return flattener.parts();
    }

    /**
     * Replaces components substrings to another components by pattern.
     * Replacements processed with the following rules: <br>
     * > inserted components share their style (text color and decorations) with following components <br>
     * > inserted components uses previous click/hover events, fonts and insertion, if they don't have own <br>
     * > inserted components do NOT share their click/hover events, fonts and insertions with the following components <br>
     * <br>
     * If replaceFunction returns null, matched string won't be replaced.
     * Processed component should have ending space as a trick for keeping ending styles (for example, component from
     * legacy string "&6&lPrefix &e")
     * @author Brikster
     * @param componentWithEndingSpace the component with ending space
     * @param pattern the pattern to match
     * @param replaceFunction the function to process a matched string
     * @return the processed component
     */
    public Component replaceWithEndingSpace(Component componentWithEndingSpace, Pattern pattern, Function<String, @Nullable Component> replaceFunction) {
        List<ComponentPart> originalParts = parts(componentWithEndingSpace);
        Component resultComponent = Component.empty();

        ComponentState state = new ComponentState();

        for (ComponentPart part : originalParts) {
            state.apply(part);

            int beginIndex = 0;
            Matcher matcher = pattern.matcher(part.text);
            while (matcher.find()) {
                String group = matcher.group();
                Component replaced = replaceFunction.apply(group);
                if (replaced != null) {
                    String previousText = part.text.substring(beginIndex, matcher.start());
                    beginIndex = matcher.end();
                    resultComponent = resultComponent
                            .append(Component.text(previousText, state.toStyle()));
                    List<ComponentPart> parts = parts(replaced);
                    for (int i = 0; i < parts.size(); i++) {
                        ComponentPart replacedComponentPart = parts.get(i);
                        state.applyForeign(replacedComponentPart);
                        String text = replacedComponentPart.text;
                        if (i == parts.size() - 1) {
                            if (text.equals(" ")) {
                                break;
                            }
                            if (text.endsWith(" ")) {
                                text = text.substring(0, text.length() - 1);
                            }
                        }
                        resultComponent = resultComponent.append(Component.text(text, state.toStyle()));
                    }
                }
            }
            if (beginIndex != part.text.length()) {
                resultComponent = resultComponent
                        .append(Component.text(part.text.substring(beginIndex), state.toStyle()));
            }
        }

        return resultComponent;
    }

}

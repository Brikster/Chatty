package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class ChattyMessages {

    private static final String ACTION_BAR_PREFIX = "actionbar!";

    /**
     * Delivers a message from a language file, honouring a leading
     * {@code actionbar!} which asks for the action bar instead of chat.
     *
     * <p>Only text a server owner wrote is read this way. A player's own message
     * never passes through here, so nobody can hand themselves the action bar by
     * typing the prefix.
     */
    public void send(@NotNull Audience audience, @NotNull Component message) {
        Component onActionBar = withoutPrefix(message, ACTION_BAR_PREFIX);
        if (onActionBar == null) {
            audience.sendMessage(message);
        } else {
            audience.sendActionBar(onActionBar);
        }
    }

    private @Nullable Component withoutPrefix(Component message, String prefix) {
        String leading = leadingText(message);
        if (!leading.startsWith(prefix)) {
            return null;
        }

        String marker = leading.startsWith(prefix + " ") ? prefix + " " : prefix;
        return message.replaceText(builder -> builder
                .matchLiteral(marker)
                .once()
                .replacement(""));
    }

    /**
     * The text the message starts with, without serialising the whole tree:
     * these are checked on every language message that goes out.
     */
    private String leadingText(Component message) {
        if (message instanceof TextComponent) {
            String content = ((TextComponent) message).content();
            if (!content.isEmpty()) {
                return content;
            }
        }
        for (Component child : message.children()) {
            String content = leadingText(child);
            if (!content.isEmpty()) {
                return content;
            }
        }
        return "";
    }

}

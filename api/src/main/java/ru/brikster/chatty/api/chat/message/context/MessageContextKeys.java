package ru.brikster.chatty.api.chat.message.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MessageContextKeys {

    /**
     * Every player the message is being delivered to, as opposed to
     * {@link MessageContext#getRecipients()}, which during the POST stage
     * holds only the single recipient the context is being rendered for.
     */
    public static final String ALL_RECIPIENTS = "all-recipients";

    /**
     * Players receiving the message because they have spy mode enabled,
     * present only when the chat enables spy and somebody is watching.
     */
    public static final String SPY_RECIPIENTS = "spy-recipients";

    public static boolean has(@NotNull MessageContext<?> context, @NotNull String key) {
        return context.getMetadata().containsKey(key);
    }

    public static @NotNull List<Player> getPlayers(@NotNull MessageContext<?> context, @NotNull String key) {
        Object value = context.getMetadata().get(key);
        if (!(value instanceof Collection)) {
            return Collections.emptyList();
        }

        List<Player> players = new ArrayList<>();
        for (Object element : (Collection<?>) value) {
            if (element instanceof Player) {
                players.add((Player) element);
            }
        }
        return players;
    }

    public static void putPlayers(@NotNull MessageContext<?> context, @NotNull String key,
                                  @NotNull Collection<? extends Player> players) {
        context.getMetadata().put(key, players);
    }

    private MessageContextKeys() {
    }

}

package ru.brikster.chatty.chat.style;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.ChatStyle;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public final class ChatStylePlayerGrouperImpl implements ChatStylePlayerGrouper {

    @Override
    public @NotNull ChatStylePlayerGrouper.Groping makeGrouping(@NotNull Collection<? extends @NotNull Player> recipients,
                                                                @NotNull Set<@NotNull ChatStyle> styles,
                                                                @Nullable Collection<? extends @NotNull Player> spies,
                                                                @Nullable ChatStyle spyStyle) {
        Map<Player, ChatStyle> playerStyleMap = new HashMap<>();

        for (ChatStyle style : styles) {
            for (Player recipient : recipients) {
                ChatStyle currentStyle = playerStyleMap.get(recipient);
                if (currentStyle == null || style.priority() > currentStyle.priority()) {
                    if (recipient.hasPermission("chatty.style." + style.id())) {
                        playerStyleMap.put(recipient, style);
                    }
                }
            }
        }

        if (spies != null) {
            spies.forEach(spyPlayer -> playerStyleMap.put(spyPlayer, spyStyle));
        }

        Map<ChatStyle, List<Player>> stylePlayersMap = new HashMap<>();

        for (var entry : playerStyleMap.entrySet()) {
            stylePlayersMap.compute(entry.getValue(), (k, v) -> {
                List<Player> players = v;
                if (players == null) {
                    players = new ArrayList<>();
                }
                players.add(entry.getKey());
                return players;
            });
        }

        List<Player> noStyleRecipients = new ArrayList<>();
        for (Player recipient : recipients) {
            if (!playerStyleMap.containsKey(recipient)) {
                noStyleRecipients.add(recipient);
            }
        }

        return new Groping(noStyleRecipients, stylePlayersMap);
    }

}

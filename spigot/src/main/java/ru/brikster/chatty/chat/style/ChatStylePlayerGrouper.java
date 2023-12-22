package ru.brikster.chatty.chat.style;

import lombok.Value;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.ChatStyle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChatStylePlayerGrouper {

    @Value
    class Groping {
        List<Player> noStylePlayers;
        Map<ChatStyle, List<Player>> stylesMap;
    }

    @NotNull ChatStylePlayerGrouper.Groping makeGrouping(@NotNull Collection<? extends @NotNull Player> recipients,
                                                         @NotNull Set<@NotNull ChatStyle> styles,
                                                         @Nullable Collection<? extends @NotNull Player> spies,
                                                         @Nullable ChatStyle spyStyle);

}

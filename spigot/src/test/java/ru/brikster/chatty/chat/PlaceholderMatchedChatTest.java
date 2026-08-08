package ru.brikster.chatty.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.chat.component.impl.ReplacementsStringTransformer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceholderMatchedChatTest {

    private static final Map<String, String> CLANS = Map.of(
            "Steve", "wolves",
            "Alex", "wolves",
            "Herobrine", "ravens");

    private final ReplacementsStringTransformer clanPlaceholder = (player, text) ->
            text.replace("%clan_name%", CLANS.getOrDefault(player.getName(), ""));

    @Test
    void deliversOnlyToPlayersSharingTheSendersValue() {
        Predicate<Player> predicate = chat("%clan_name%").getRecipientPredicate(player("Steve"));

        assertTrue(predicate.test(player("Alex")), "same clan must receive");
        assertFalse(predicate.test(player("Herobrine")), "another clan must not receive");
    }

    @Test
    void alwaysDeliversToTheSenderThemselves() {
        Player steve = player("Steve");
        assertTrue(chat("%clan_name%").getRecipientPredicate(steve).test(steve));
    }

    @Test
    void deliversToEveryoneWhenNoPlaceholderIsConfigured() {
        Predicate<Player> predicate = chat("").getRecipientPredicate(player("Steve"));

        assertTrue(predicate.test(player("Alex")));
        assertTrue(predicate.test(player("Herobrine")));
    }

    private ChatImpl chat(String matchPlaceholder) {
        return new ChatImpl("clan", "Clan", mock(net.kyori.adventure.platform.bukkit.BukkitAudiences.class),
                Component.empty(), "{original-message}", "", null,
                -2, false, Set.of(), false, false, false, null, Component.empty(), 0,
                null, List.of(), matchPlaceholder, clanPlaceholder);
    }

    private static Player player(String name) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(name);
        return player;
    }

}

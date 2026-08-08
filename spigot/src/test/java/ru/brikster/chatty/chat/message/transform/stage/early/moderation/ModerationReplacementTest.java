package ru.brikster.chatty.chat.message.transform.stage.early.moderation;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.ModerationConfig;
import ru.brikster.chatty.config.file.ModerationConfig.AdvertisementModerationConfig;

import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModerationReplacementTest {

    @Test
    void treatsADollarSignInTheReplacementAsText() {
        AdModerationStrategyModeration strategy = strategyWithReplacement("[$1 blocked]");

        MessageContext<String> context = contextWith("visit 192.168.0.1 now");

        MessageTransformResult<String> result =
                assertDoesNotThrow(() -> strategy.handle(context),
                        "a $ in moderation.yml replacement must not break chat");

        assertEquals("visit [$1 blocked] now", result.getNewContext().getMessage());
    }

    @Test
    void treatsABackslashInTheReplacementAsText() {
        AdModerationStrategyModeration strategy = strategyWithReplacement("[ad\\]");

        MessageContext<String> context = contextWith("visit 192.168.0.1 now");

        MessageTransformResult<String> result =
                assertDoesNotThrow(() -> strategy.handle(context),
                        "a backslash in moderation.yml replacement must not break chat");

        assertEquals("visit [ad\\] now", result.getNewContext().getMessage());
    }

    @Test
    void survivesAReplacementEndingInADollarSign() {
        AdModerationStrategyModeration strategy = strategyWithReplacement("costs $");

        MessageContext<String> context = contextWith("visit 192.168.0.1 now");

        MessageTransformResult<String> result =
                assertDoesNotThrow(() -> strategy.handle(context),
                        "a trailing $ in the replacement must not break every message on the server");

        assertEquals("visit costs $ now", result.getNewContext().getMessage());
    }

    private AdModerationStrategyModeration strategyWithReplacement(String replacement) {
        AdvertisementModerationConfig advertisement = mock(AdvertisementModerationConfig.class);
        when(advertisement.getWhitelist()).thenReturn(Set.of());
        when(advertisement.isBlock()).thenReturn(false);
        when(advertisement.getReplacement()).thenReturn(replacement);
        when(advertisement.getIpPattern()).thenReturn(Pattern.compile("\\b\\d{1,3}(\\.\\d{1,3}){3}\\b"));
        when(advertisement.getLinkPattern()).thenReturn(Pattern.compile("\\bhttps?://\\S+\\b"));

        ModerationConfig moderationConfig = mock(ModerationConfig.class);
        when(moderationConfig.getAdvertisement()).thenReturn(advertisement);

        BukkitAudiences audiences = mock(BukkitAudiences.class);
        when(audiences.player(any(Player.class))).thenReturn(mock(Audience.class));

        return new AdModerationStrategyModeration(audiences, mock(MessagesConfig.class), moderationConfig);
    }

    @SuppressWarnings("unchecked")
    private MessageContext<String> contextWith(String message) {
        Player sender = mock(Player.class);
        MessageContext<String> context = mock(MessageContext.class);
        when(context.getSender()).thenReturn(sender);
        when(context.getMessage()).thenReturn(message);
        return context;
    }

}

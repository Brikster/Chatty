package ru.brikster.chatty.notification;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActionbarNotificationSoundTest {

    private static final Sound SOUND =
            Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

    @Test
    void playsTheSoundOncePerMessageRatherThanEveryTick() {
        Player player = mock(Player.class);
        Audience audience = mock(Audience.class);

        BukkitAudiences audiences = mock(BukkitAudiences.class);
        when(audiences.player(player)).thenReturn(audience);

        PlaceholdersComponentTransformer transformer = mock(PlaceholdersComponentTransformer.class);
        doReturn(Component.text("message"))
                .when(transformer).transform(any(Component.class), any(SinglePlayerTransformContext.class));

        int period = 10;
        int stay = 5;
        ActionbarNotification notification = new ActionbarNotification("default", period, stay,
                List.of(Component.text("first"), Component.text("second")),
                false, false, SOUND, audiences, transformer);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            //noinspection unchecked,rawtypes
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(player));

            for (int i = 0; i < period * 2; i++) {
                notification.tick();
            }
        }

        verify(audience, times(2)).playSound(SOUND);
        verify(audience, times(2 * (stay - 1))).sendActionBar(any(Component.class));
    }

    @Test
    void playsNoSoundWhenNoneIsConfigured() {
        Player player = mock(Player.class);
        Audience audience = mock(Audience.class);

        BukkitAudiences audiences = mock(BukkitAudiences.class);
        when(audiences.player(player)).thenReturn(audience);

        PlaceholdersComponentTransformer transformer = mock(PlaceholdersComponentTransformer.class);
        doReturn(Component.text("message"))
                .when(transformer).transform(any(Component.class), any(SinglePlayerTransformContext.class));

        ActionbarNotification notification = new ActionbarNotification("default", 10, 5,
                List.of(Component.text("first")),
                false, false, null, audiences, transformer);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            //noinspection unchecked,rawtypes
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn((List) List.of(player));

            for (int i = 0; i < 20; i++) {
                notification.tick();
            }
        }

        verify(audience, times(0)).playSound(any(Sound.class));
    }

}

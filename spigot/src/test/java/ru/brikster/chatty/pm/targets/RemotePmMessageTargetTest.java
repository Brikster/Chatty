package ru.brikster.chatty.pm.targets;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class RemotePmMessageTargetTest {

    @Test
    void resolvesByNameWhenTheProxyDoesNotKnowTheUuid() {
        OfflinePlayer byName = mock(OfflinePlayer.class);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getOfflinePlayer("Steve")).thenReturn(byName);

            RemotePmMessageTarget target = new RemotePmMessageTarget("Steve", null);

            OfflinePlayer resolved = assertDoesNotThrow(target::asOfflinePlayer,
                    "a proxy target with an unknown UUID must not break /msg");
            assertSame(byName, resolved);
        }
    }

    @Test
    void resolvesByUuidWhenTheProxyKnowsIt() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        OfflinePlayer byUuid = mock(OfflinePlayer.class);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getOfflinePlayer(uuid)).thenReturn(byUuid);

            RemotePmMessageTarget target = new RemotePmMessageTarget("Steve", uuid);

            assertSame(byUuid, target.asOfflinePlayer());
        }
    }

}

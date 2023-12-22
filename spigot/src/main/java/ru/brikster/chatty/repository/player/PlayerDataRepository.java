package ru.brikster.chatty.repository.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface PlayerDataRepository extends AutoCloseable {

    @NotNull Set<@NotNull UUID> getWhoIgnoreUuids(@NotNull Player player);

    @NotNull Set<@NotNull UUID> getIgnoredPlayersByUuids(@NotNull Player player);

    @NotNull Set<@NotNull String> getIgnoredPlayersByUsernames(@NotNull Player player);

    void createOrUpdateUser(@NotNull UUID uuid, @NotNull String username);

    @Nullable UUID getCachedUuid(@NotNull String playerName);

    @Nullable String getCachedUsername(@NotNull UUID uuid);

    void addIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid);

    void removeIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid);

    boolean isIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid);

}

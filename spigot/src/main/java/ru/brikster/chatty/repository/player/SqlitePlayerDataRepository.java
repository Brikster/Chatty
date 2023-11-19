package ru.brikster.chatty.repository.player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.util.SqliteUtil;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Singleton
public final class SqlitePlayerDataRepository implements PlayerDataRepository {

    private final HikariDataSource dataSource;

    public SqlitePlayerDataRepository(Path dataFolder) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dataFolder.resolve("database.sqlite").toAbsolutePath());
        config.setPoolName("Chatty");
        config.setMaximumPoolSize(8);

        this.dataSource = new HikariDataSource(config);

        Flyway flyway = Flyway.configure(Chatty.class.getClassLoader())
                .dataSource(dataSource)
                .load();
        flyway.migrate();
    }

    @Override
    public @NotNull Set<@NotNull UUID> getWhoIgnoreUuids(@NotNull Player player) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT player_uuid " +
                             "FROM ignored_users " +
                             "WHERE ignored_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(player.getUniqueId()));

            ResultSet resultSet = statement.executeQuery();

            Set<UUID> ignoredPlayers = new HashSet<>();
            while (resultSet.next()) {
                ignoredPlayers.add(SqliteUtil.toUUID(resultSet.getBytes(1)));
            }

            return ignoredPlayers;
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot retrieve ignored players", sqlException);
        }
    }

    @Override
    public @NotNull Set<@NotNull UUID> getIgnoredPlayersByUuids(@NotNull Player player) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT ignored_uuid " +
                             "FROM ignored_users " +
                             "WHERE player_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(player.getUniqueId()));

            ResultSet resultSet = statement.executeQuery();

            Set<UUID> ignoredPlayers = new HashSet<>();
            while (resultSet.next()) {
                ignoredPlayers.add(SqliteUtil.toUUID(resultSet.getBytes(1)));
            }

            return ignoredPlayers;
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot retrieve ignored players", sqlException);
        }
    }

    @Override
    public @NotNull Set<@NotNull String> getIgnoredPlayersByUsernames(@NotNull Player player) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT username " +
                             "FROM ignored_users iu JOIN users u ON iu.ignored_uuid = u.uuid " +
                             "WHERE player_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(player.getUniqueId()));

            ResultSet resultSet = statement.executeQuery();

            Set<String> ignoredPlayers = new HashSet<>();
            while (resultSet.next()) {
                ignoredPlayers.add(resultSet.getString(1));
            }

            return ignoredPlayers;
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot retrieve ignored players", sqlException);
        }
    }

    @Override
    public void createOrUpdateUser(@NotNull UUID uuid, @NotNull String username) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (uuid, username) VALUES (?, ?) ON CONFLICT (uuid) DO UPDATE SET username = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(uuid));
            statement.setString(2, username);
            statement.setString(3, username);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot create or update", sqlException);
        }
    }

    @Override
    public @Nullable UUID getCachedUuid(@NotNull String playerName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid " +
                             "FROM users " +
                             "WHERE lower(username) = lower(?)")) {
            statement.setString(1, playerName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return SqliteUtil.toUUID(resultSet.getBytes(1));
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot retrieve cached uuid", sqlException);
        }
    }

    @Override
    public @Nullable String getCachedUsername(@NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT username " +
                             "FROM users " +
                             "WHERE uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(uuid));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot retrieve cached username", sqlException);
        }
    }

    @Override
    public void addIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO ignored_users (player_uuid, ignored_uuid) VALUES (?, ?)")) {
            statement.setBytes(1, SqliteUtil.fromUUID(player.getUniqueId()));
            statement.setBytes(2, SqliteUtil.fromUUID(uuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot add ignored player", sqlException);
        }
    }

    @Override
    public void removeIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM ignored_users" +
                             " WHERE player_uuid = ? AND ignored_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(player.getUniqueId()));
            statement.setBytes(2, SqliteUtil.fromUUID(uuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot remove ignored player", sqlException);
        }
    }

    @Override
    public boolean isIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT ignored_uuid " +
                             "FROM ignored_users " +
                             "WHERE player_uuid = ? AND ignored_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(player.getUniqueId()));
            statement.setBytes(2, SqliteUtil.fromUUID(uuid));

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot check ignored player", sqlException);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

}

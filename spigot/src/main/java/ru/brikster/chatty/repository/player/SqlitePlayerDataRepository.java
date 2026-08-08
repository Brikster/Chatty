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
import java.io.IOException;
import java.nio.file.Files;
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
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data folder", e);
        }

        String jdbcUrl = "jdbc:sqlite:" + dataFolder.resolve("database.sqlite");

        HikariConfig config = new HikariConfig();
        // Use the bundled driver loaded by an isolated classloader instead of a
        // bare jdbcUrl — otherwise HikariCP picks up whatever org.sqlite the
        // server ships, which on legacy servers is an ancient, broken build.
        config.setDataSource(IsolatedSqliteDriver.createDataSource(dataFolder, jdbcUrl));
        config.setPoolName("Chatty");
        config.setMaximumPoolSize(8);

        this.dataSource = new HikariDataSource(config);

        Flyway flyway = Flyway.configure(Chatty.class.getClassLoader())
                .locations("db/migration/sqlite")
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
    public void addIgnoredPlayer(@NotNull UUID playerUuid, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO ignored_users (player_uuid, ignored_uuid) VALUES (?, ?)")) {
            statement.setBytes(1, SqliteUtil.fromUUID(playerUuid));
            statement.setBytes(2, SqliteUtil.fromUUID(uuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot add ignored player", sqlException);
        }
    }

    @Override
    public void removeIgnoredPlayer(@NotNull UUID playerUuid, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM ignored_users" +
                             " WHERE player_uuid = ? AND ignored_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(playerUuid));
            statement.setBytes(2, SqliteUtil.fromUUID(uuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot remove ignored player", sqlException);
        }
    }

    @Override
    public boolean isIgnoredPlayer(@NotNull UUID playerUuid, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT ignored_uuid " +
                             "FROM ignored_users " +
                             "WHERE player_uuid = ? AND ignored_uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(playerUuid));
            statement.setBytes(2, SqliteUtil.fromUUID(uuid));

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot check ignored player", sqlException);
        }
    }

    @Override
    public boolean isEnableSpy(@NotNull UUID playerUuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT spy " +
                             "FROM users " +
                             "WHERE uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(playerUuid));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("spy");
            } else {
                return false;
            }
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot check player spy mode", sqlException);
        }
    }

    @Override
    public void setEnableSpy(@NotNull UUID playerUuid, boolean spy) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET spy = ? WHERE uuid = ?")) {
            statement.setBoolean(1, spy);
            statement.setBytes(2, SqliteUtil.fromUUID(playerUuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot update player spy mode", sqlException);
        }
    }

    @Override
    public @NotNull Set<@NotNull UUID> getSpyEnabledUuids() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid " +
                             "FROM users " +
                             "WHERE spy = 1")) {
            ResultSet resultSet = statement.executeQuery();

            Set<UUID> spies = new HashSet<>();
            while (resultSet.next()) {
                spies.add(SqliteUtil.toUUID(resultSet.getBytes(1)));
            }

            return spies;
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot retrieve spies", sqlException);
        }
    }

    @Override
    public @Nullable Mute getMute(@NotNull UUID playerUuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT muted_until, mute_reason FROM users WHERE uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(playerUuid));

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            long until = resultSet.getLong(1);
            if (resultSet.wasNull()) {
                return null;
            }
            return new Mute(until, resultSet.getString(2));
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot read player mute", sqlException);
        }
    }

    @Override
    public void setMute(@NotNull UUID playerUuid, @NotNull Mute mute) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET muted_until = ?, mute_reason = ? WHERE uuid = ?")) {
            statement.setLong(1, mute.getUntil());
            statement.setString(2, mute.getReason());
            statement.setBytes(3, SqliteUtil.fromUUID(playerUuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot store player mute", sqlException);
        }
    }

    @Override
    public void clearMute(@NotNull UUID playerUuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET muted_until = NULL, mute_reason = NULL WHERE uuid = ?")) {
            statement.setBytes(1, SqliteUtil.fromUUID(playerUuid));
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot clear player mute", sqlException);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

}

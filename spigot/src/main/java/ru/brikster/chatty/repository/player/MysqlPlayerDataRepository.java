package ru.brikster.chatty.repository.player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.config.file.ProxyConfig.DatabaseConfig;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Singleton
public final class MysqlPlayerDataRepository implements PlayerDataRepository {

    private final HikariDataSource dataSource;

    public MysqlPlayerDataRepository(DatabaseConfig databaseConfig) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                databaseConfig.getHostname(), databaseConfig.getPort(), databaseConfig.getDatabase()));
        config.addDataSourceProperty("user", databaseConfig.getUsername());
        config.addDataSourceProperty("password", databaseConfig.getPassword());
        config.setPoolName("Chatty");
        config.setMaximumPoolSize(8);

        this.dataSource = new HikariDataSource(config);

        Flyway flyway = Flyway.configure(Chatty.class.getClassLoader())
                .locations("db/migration/mysql")
                .dataSource(dataSource)
                .load();
        flyway.migrate();
    }

    @Override
    public @NotNull Set<@NotNull UUID> getWhoIgnoreUuids(@NotNull Player player) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT player_uuid " +
                             "FROM chatty_ignored_users " +
                             "WHERE ignored_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = statement.executeQuery();

            Set<UUID> ignoredPlayers = new HashSet<>();
            while (resultSet.next()) {
                ignoredPlayers.add(UUID.fromString(resultSet.getString(1)));
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
                             "FROM chatty_ignored_users " +
                             "WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = statement.executeQuery();

            Set<UUID> ignoredPlayers = new HashSet<>();
            while (resultSet.next()) {
                ignoredPlayers.add(UUID.fromString(resultSet.getString(1)));
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
                             "FROM chatty_ignored_users iu JOIN chatty_users u ON iu.ignored_uuid = u.uuid " +
                             "WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());

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
                     "INSERT INTO chatty_users (uuid, username) VALUES (?, ?) " +
                             "ON DUPLICATE KEY UPDATE username = ?")) {
            statement.setString(1, uuid.toString());
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
                             "FROM chatty_users " +
                             "WHERE lower(username) = lower(?)")) {
            statement.setString(1, playerName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString(1));
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
                             "FROM chatty_users " +
                             "WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

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
                     "INSERT INTO chatty_ignored_users (player_uuid, ignored_uuid) VALUES (?, ?)")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new IllegalStateException("Cannot add ignored player", sqlException);
        }
    }

    @Override
    public void removeIgnoredPlayer(@NotNull Player player, @NotNull UUID uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM chatty_ignored_users" +
                             " WHERE player_uuid = ? AND ignored_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, uuid.toString());
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
                             "FROM chatty_ignored_users " +
                             "WHERE player_uuid = ? AND ignored_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, uuid.toString());

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

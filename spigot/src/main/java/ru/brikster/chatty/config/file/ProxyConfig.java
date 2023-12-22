package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.brikster.chatty.BuildConstants;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Header("################################################################")
@Header("#")
@Header("#    Chatty (version " + BuildConstants.VERSION + ")")
@Header("#    Author: Brikster")
@Header("#")
@Header("################################################################")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ProxyConfig extends OkaeriConfig {

    @Comment({"", "Enable support for cross-server messaging?"})
    @Comment("This feature supports any proxy (BungeeCord, Velocity), ")
    @Comment("including multi-proxy networks.")
    @Comment("You need to setup Redis and shared database to use it.")
    @Comment
    @Comment("Note: relational placeholders will not be parsed for cross-server messages.")
    @Comment("Some placeholders of message target (in PM) also may not work (if plugin doesn't support")
    @Comment("placeholders for offline players)")
    private boolean enable = false;

    @Comment({"", "Basic Redis configuration."})
    private RedisConfig redisConfig = new RedisConfig();

    @Comment({"", "Enable external Redis configuration for advanced setup."})
    @Comment("File with name \"redis_config.json\" will be created.")
    @Comment("See \"Redisson\" library documentation for details")
    private boolean useExternalRedisConfig = false;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class RedisConfig extends OkaeriConfig {

        private String address = "redis://localhost:6379";
        private String username = "";
        private String password = "";

    }

    @Comment({"", "Shared database configuration"})
    @Comment("(database should be the same for every server).")
    @Comment("Possible types: POSTGRESQL, MYSQL.")
    @Comment("Default port for PostgreSQL: 5432, for MySQL: 3306")
    private DatabaseConfig databaseConfig = new DatabaseConfig();

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class DatabaseConfig extends OkaeriConfig {

        public enum DatasourceType {
            MYSQL, POSTGRESQL
        }

        private DatasourceType type = DatasourceType.MYSQL;
        private String hostname = "localhost";
        private int port = 3306;
        private String database = "app";
        private String username = "app";
        private String password = "12345";

    }

}

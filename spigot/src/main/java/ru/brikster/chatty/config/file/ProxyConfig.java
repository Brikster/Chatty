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

    @Comment(value = {
            "",
            "Enable support for cross-server messaging?"}, language = "en-US")
    @Comment(value = {
            "",
            "Включить поддержку межсерверных сообщений?"}, language = "ru-RU")
    @Comment(value = "This feature supports any proxy (BungeeCord, Velocity), ", language = "en-US")
    @Comment(value = "Работает с любым прокси (BungeeCord, Velocity), ", language = "ru-RU")
    @Comment(value = "including multi-proxy networks.", language = "en-US")
    @Comment(value = "в том числе с сетями из нескольких прокси.", language = "ru-RU")
    @Comment(value = "You need to setup Redis and shared database to use it.", language = "en-US")
    @Comment(value = "Для работы нужны Redis и общая база данных.", language = "ru-RU")
    @Comment
    @Comment(value = "Note: relational placeholders will not be parsed for cross-server messages.", language = "en-US")
    @Comment(value = "Учтите: относительные плейсхолдеры для межсерверных сообщений не обрабатываются.", language = "ru-RU")
    @Comment(value = "Some placeholders of message target (in PM) also may not work (if plugin doesn't support", language = "en-US")
    @Comment(value = "Некоторые плейсхолдеры получателя (в ЛС) тоже могут не работать (если плагин не умеет", language = "ru-RU")
    @Comment(value = "placeholders for offline players)", language = "en-US")
    @Comment(value = "плейсхолдеры для офлайн-игроков)", language = "ru-RU")
    private boolean enable = false;

    @Comment(value = {
            "",
            "Basic Redis configuration."}, language = "en-US")
    @Comment(value = {
            "",
            "Базовая настройка Redis."}, language = "ru-RU")
    private RedisConfig redisConfig = new RedisConfig();

    @Comment(value = {
            "",
            "Enable external Redis configuration for advanced setup."}, language = "en-US")
    @Comment(value = {
            "",
            "Включите внешнюю настройку Redis для тонкой конфигурации."}, language = "ru-RU")
    @Comment(value = "File with name \"redis_config.json\" will be created.", language = "en-US")
    @Comment(value = "Будет создан файл \"redis_config.json\".", language = "ru-RU")
    @Comment(value = "See \"Redisson\" library documentation for details", language = "en-US")
    @Comment(value = "Подробности — в документации библиотеки \"Redisson\"", language = "ru-RU")
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

    @Comment(value = {
            "",
            "Shared database configuration"}, language = "en-US")
    @Comment(value = {
            "",
            "Настройка общей базы данных"}, language = "ru-RU")
    @Comment(value = "(database should be the same for every server).", language = "en-US")
    @Comment(value = "(база должна быть одна для всех серверов).", language = "ru-RU")
    @Comment(value = "Possible types: POSTGRESQL, MYSQL.", language = "en-US")
    @Comment(value = "Возможные типы: POSTGRESQL, MYSQL.", language = "ru-RU")
    @Comment(value = "Default port for PostgreSQL: 5432, for MySQL: 3306", language = "en-US")
    @Comment(value = "Порт по умолчанию: PostgreSQL — 5432, MySQL — 3306", language = "ru-RU")
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

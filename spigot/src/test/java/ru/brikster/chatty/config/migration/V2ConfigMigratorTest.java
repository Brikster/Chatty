package ru.brikster.chatty.config.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V2ConfigMigratorTest {

    @TempDir
    Path dataFolder;

    private static final String LEGACY_CONFIG = String.join("\n",
            "config-version: '2.0'",
            "general:",
            "  priority: high",
            "  keep-old-recipients: false",
            "  hide-vanished-recipients: true",
            "chats:",
            "  local:",
            "    enable: true",
            "    display-name: Local",
            "    format: '&e[Local] {player}: {message}'",
            "    range: 100",
            "    cooldown: -1",
            "    permission: false",
            "  global:",
            "    enable: true",
            "    format: '&e[Global] {player}: {message}'",
            "    range: -3",
            "    symbol: '!'",
            "  staff:",
            "    enable: false",
            "    format: '&cStaff'",
            "spy:",
            "  format:",
            "    chat: '&6[Spy] &r{format}'",
            "pm:",
            "  enable: true",
            "  allow-console: true",
            "  format:",
            "    sender: '&7{sender-name} -> {recipient-name}: {message}'",
            "    recipient: '&7{sender-name} -> {recipient-name}: {message}'",
            "json:",
            "  mentions:",
            "    enable: true",
            "    format: '&e&l@{player}'",
            "moderation:",
            "  caps:",
            "    enable: false",
            "    length: 10",
            "    percent: 70",
            "    block: false",
            "  advertisement:",
            "    enable: true",
            "    block: false",
            "    replacement: '[ad]'",
            "    patterns:",
            "      ip: 'IP_REGEX'",
            "      web: 'WEB_REGEX'",
            "    whitelist:",
            "      - example.com",
            "  swear:",
            "    enable: true",
            "    block: true",
            "    replacement: '[swear]'",
            "miscellaneous:",
            "  vanilla:",
            "    join:",
            "      enable: false",
            "      message: '&aJoined {player}'",
            "      permission: true",
            "      first-join:",
            "        message: '&aFirst {player}'",
            "    quit:",
            "      enable: true",
            "      message: '&cLeft {player}'",
            "    death:",
            "      enable: true",
            "      message: '&cDied {player}'");

    /** Minimal stand-ins for the v3 files okaeri generates with defaults. */
    @BeforeEach
    void writeDefaultV3Files() throws Exception {
        write("chats.yml", String.join("\n",
                "chats:",
                "  local:",
                "    format: default"));
        write("settings.yml", String.join("\n",
                "listener-priority: LOW",
                "respect-foreign-recipients: true",
                "hide-vanished-recipients: false",
                "mentions:",
                "  enable: true",
                "  target-format: default"));
        write("moderation.yml", String.join("\n",
                "caps:",
                "  enable: true",
                "  length: 6",
                "  percent: 80",
                "  block: true",
                "advertisement:",
                "  enable: true",
                "  block: true",
                "  replacement: <advertisement>",
                "  whitelist: []",
                "swear:",
                "  enable: true",
                "  block: true",
                "  replacement: <swear>"));
        write("vanilla.yml", String.join("\n",
                "join:",
                "  enable: true",
                "  message: default",
                "  permission-required: false",
                "  first-join:",
                "    enable: true",
                "    message: default",
                "quit:",
                "  enable: true",
                "  message: default",
                "  permission-required: false",
                "death:",
                "  enable: true",
                "  message: default",
                "  permission-required: false"));
        write("pm.yml", String.join("\n",
                "enable: true",
                "allow-console: false",
                "from-format: default",
                "to-format: default"));
    }

    private V2ConfigMigratorTest runMigration() throws Exception {
        return runMigration(LEGACY_CONFIG);
    }

    private V2ConfigMigratorTest runMigration(String legacyConfig) throws Exception {
        Path legacyFile = dataFolder.resolve("config.yml");
        write("config.yml", legacyConfig);
        Map<String, Object> legacy = V2ConfigMigrator.readLegacyConfig(legacyFile);
        assertNotNull(legacy, "legacy config should be readable");
        new V2ConfigMigrator(Logger.getLogger("test")).migrate(legacy, dataFolder);
        return this;
    }

    @Test
    void migratesChatsPreservingRangeAndClampingCooldown() throws Exception {
        runMigration();
        Map<String, Object> chats = childMap(read("chats.yml"), "chats");

        Map<String, Object> local = childMap(chats, "local");
        assertEquals(100, local.get("range"));
        assertEquals(0, local.get("cooldown"), "v2 cooldown -1 must clamp to 0");
        assertEquals(false, local.get("permission-required"));
        assertTrue(String.valueOf(local.get("format")).contains("[Local]"));

        Map<String, Object> global = childMap(chats, "global");
        assertEquals(-3, global.get("range"), "v2 range -3 is cross-server chat and must survive");
        assertEquals("!", global.get("symbol"));
        assertEquals(true, global.get("permission-required"), "v2 permission defaults to true");
    }

    @Test
    void skipsChatsDisabledInV2() throws Exception {
        runMigration();
        Map<String, Object> chats = childMap(read("chats.yml"), "chats");
        assertFalse(chats.containsKey("staff"), "disabled v2 chat must not be migrated");
    }

    @Test
    void expandsSpyFormatPlaceholder() throws Exception {
        runMigration();
        Map<String, Object> chats = childMap(read("chats.yml"), "chats");
        Map<String, Object> spy = childMap(childMap(chats, "local"), "spy");
        assertEquals("&6[Spy] &r&e[Local] {player}: {message}", spy.get("format"));
    }

    @Test
    void writesASpyFormatEvenWhenV2HadNoSpySection() throws Exception {
        runMigration(LEGACY_CONFIG.replace(
                String.join("\n",
                        "spy:",
                        "  format:",
                        "    chat: '&6[Spy] &r{format}'"),
                ""));

        Map<String, Object> chats = childMap(read("chats.yml"), "chats");
        for (String chatId : List.of("local", "global")) {
            Map<String, Object> spy = childMap(childMap(chats, chatId), "spy");
            Object format = spy.get("format");
            assertNotNull(format, "chat \"" + chatId + "\" migrated with a null spy format");
            assertFalse(String.valueOf(format).isBlank(),
                    "chat \"" + chatId + "\" migrated with a blank spy format");
        }
    }

    @Test
    void migratesSettingsAndMentions() throws Exception {
        runMigration();
        Map<String, Object> settings = read("settings.yml");
        assertEquals("HIGH", settings.get("listener-priority"));
        assertEquals(false, settings.get("respect-foreign-recipients"));

        Map<String, Object> mentions = childMap(settings, "mentions");
        assertEquals("&e&l@{username}", mentions.get("target-format"),
                "v2 {player} must become v3 {username}");
    }

    @Test
    void migratesLocaleToTheV3LanguageCode() throws Exception {
        runMigration(LEGACY_CONFIG.replace(
                "general:\n  priority: high",
                "general:\n  locale: ru\n  priority: high"));
        assertEquals("ru-RU", read("settings.yml").get("language"));
    }

    @Test
    void migratesUnderscoredAndBareLocales() throws Exception {
        runMigration(LEGACY_CONFIG.replace(
                "general:\n  priority: high",
                "general:\n  locale: zh_CN\n  priority: high"));
        assertEquals("zh-CN", read("settings.yml").get("language"));
    }

    @Test
    void leavesTheLanguageAloneForAnUnknownLocale() throws Exception {
        runMigration(LEGACY_CONFIG.replace(
                "general:\n  priority: high",
                "general:\n  locale: kl_GL\n  priority: high"));
        assertFalse(read("settings.yml").containsKey("language"),
                "an unmappable locale must not be written as a bogus language");
    }

    @Test
    void migratesModeration() throws Exception {
        runMigration();
        Map<String, Object> moderation = read("moderation.yml");

        Map<String, Object> caps = childMap(moderation, "caps");
        assertEquals(false, caps.get("enable"));
        assertEquals(10, caps.get("length"));
        assertEquals(70, caps.get("percent"));

        Map<String, Object> ad = childMap(moderation, "advertisement");
        assertEquals("[ad]", ad.get("replacement"));
        assertEquals("IP_REGEX", ad.get("ip-pattern"));
        assertEquals("WEB_REGEX", ad.get("link-pattern"));
        assertTrue(((List<?>) ad.get("whitelist")).contains("example.com"));

        assertEquals("[swear]", childMap(moderation, "swear").get("replacement"));
    }

    @Test
    void clampsCapsValuesV3WouldReject() throws Exception {
        runMigration(LEGACY_CONFIG
                .replace("    length: 10", "    length: 0")
                .replace("    percent: 70", "    percent: 700"));

        Map<String, Object> caps = childMap(read("moderation.yml"), "caps");
        assertEquals(1, caps.get("length"), "@Positive rejects a length of 0");
        assertEquals(100, caps.get("percent"), "@Max(100) rejects a percent above 100");
    }

    @Test
    void migratesPmWithTranslatedPlaceholders() throws Exception {
        runMigration();
        Map<String, Object> pm = read("pm.yml");
        assertEquals(true, pm.get("allow-console"));
        assertEquals("&7{from-name} -> {to-name}: {message}", pm.get("from-format"));
        assertEquals("&7{from-name} -> {to-name}: {message}", pm.get("to-format"));
    }

    @Test
    void migratesVanillaMessages() throws Exception {
        runMigration();
        Map<String, Object> vanilla = read("vanilla.yml");

        Map<String, Object> join = childMap(vanilla, "join");
        assertEquals(false, join.get("enable"));
        assertEquals("&aJoined {player}", join.get("message"));
        assertEquals(true, join.get("permission-required"));
        assertEquals("&aFirst {player}", childMap(join, "first-join").get("message"));

        assertEquals("&cLeft {player}", childMap(vanilla, "quit").get("message"));
    }

    // ----- helpers ---------------------------------------------------------

    private void write(String fileName, String content) throws Exception {
        try (Writer writer = Files.newBufferedWriter(dataFolder.resolve(fileName), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    private Map<String, Object> read(String fileName) throws Exception {
        try (Reader reader = Files.newBufferedReader(dataFolder.resolve(fileName), StandardCharsets.UTF_8)) {
            return cast(new Yaml().load(reader));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> childMap(Map<String, Object> parent, String key) {
        return cast(parent.get(key));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Object value) {
        assertTrue(value instanceof Map, "expected a YAML mapping, got: " + value);
        return (Map<String, Object>) value;
    }

}

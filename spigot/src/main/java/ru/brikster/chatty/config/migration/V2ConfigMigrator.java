package ru.brikster.chatty.config.migration;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.brikster.chatty.config.file.SettingsConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Best-effort migration of a legacy Chatty v2 {@code config.yml} into the
 * split v3 configuration files.
 *
 * <p>The migrator only sets the values it can map unambiguously; every v3
 * config file is generated with defaults first, and this class overwrites
 * the migrated keys in place. Values it cannot safely map (sounds, JSON
 * replacements, notifications, locale files, per-chat commands) are left at
 * their v3 defaults and reported as notes for the administrator to review.
 */
public final class V2ConfigMigrator {

    private static final Set<String> VALID_PRIORITIES =
            Set.of("LOWEST", "LOW", "NORMAL", "HIGH", "HIGHEST", "MONITOR");

    private static final String DEFAULT_SPY_FORMAT = "&6[Spy] &r{format}";

    private static final String DEFAULT_SPIED_FORMAT = "{prefix}{player}{suffix}&8: &f{message}";

    private final Logger logger;
    private final List<String> notes = new ArrayList<>();

    public V2ConfigMigrator(Logger logger) {
        this.logger = logger;
    }

    /** Reads a legacy v2 {@code config.yml} into a nested map, or {@code null} on failure. */
    public static @Nullable Map<String, Object> readLegacyConfig(Path configFile) {
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            return asMap(loaded);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Migrates the given legacy config into the freshly generated v3 config
     * files inside {@code dataFolder}. Never throws — partial failures are
     * logged and collected as notes.
     */
    public void migrate(Map<String, Object> legacy, Path dataFolder) {
        logger.info("Migrating legacy Chatty v2 configuration...");

        run("chats.yml", () -> migrateChats(legacy, dataFolder));
        run("settings.yml", () -> migrateSettings(legacy, dataFolder));
        run("vanilla.yml", () -> migrateVanilla(legacy, dataFolder));
        run("moderation.yml", () -> migrateModeration(legacy, dataFolder));
        run("pm.yml", () -> migratePm(legacy, dataFolder));

        notes.add("Sounds were not migrated (v2 used Bukkit sound names) — review *.yml.");
        notes.add("Interactive replacements (json.replacements) were not migrated — see replacements.yml.");
        notes.add("Notifications were not migrated — see notifications.yml.");
        notes.add("Locale messages were not migrated — see the lang/ folder.");
        notes.add("Cross-server chat now uses Redis — see proxy.yml (the v2 BungeeCord setting is not migrated).");

        logger.info("Legacy configuration migrated. Please review the following:");
        for (String note : notes) {
            logger.log(Level.INFO, " - {0}", note);
        }
    }

    private interface MigrationStep {
        void run() throws Exception;
    }

    private void run(String fileName, MigrationStep step) {
        try {
            step.run();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not migrate " + fileName + ", leaving defaults", e);
            notes.add(fileName + " could not be migrated automatically — configure it manually.");
        }
    }

    // ----- chats.yml -------------------------------------------------------

    private void migrateChats(Map<String, Object> legacy, Path dataFolder) throws IOException {
        Map<String, Object> legacyChats = section(legacy, "chats");
        if (legacyChats == null || legacyChats.isEmpty()) {
            return;
        }

        Map<String, Object> legacySpyFormat = section(legacy, "spy", "format");
        String legacySpyChatFormat = legacySpyFormat == null ? null : str(legacySpyFormat.get("chat"));

        Map<String, Object> newChats = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : legacyChats.entrySet()) {
            Map<String, Object> legacyChat = asMap(entry.getValue());
            if (legacyChat == null) {
                continue;
            }
            if (Boolean.FALSE.equals(legacyChat.get("enable"))) {
                notes.add("chat \"" + entry.getKey() + "\" was disabled in v2 and was not migrated.");
                continue;
            }

            Map<String, Object> chat = new LinkedHashMap<>();
            if (legacyChat.get("display-name") != null) {
                chat.put("display-name", str(legacyChat.get("display-name")));
            }
            if (legacyChat.get("format") != null) {
                chat.put("format", str(legacyChat.get("format")));
            }
            chat.put("symbol", legacyChat.get("symbol") != null ? str(legacyChat.get("symbol")) : "");

            int range = intValue(legacyChat.get("range"), -2);
            if (range <= -3) {
                range = -3;
                notes.add("chat \"" + entry.getKey() + "\" is cross-server (range -3):"
                        + " configure proxy.yml, v3 uses Redis instead of BungeeCord.");
            }
            chat.put("range", range);

            // In v2 the "permission" flag defaulted to true.
            chat.put("permission-required", boolValue(legacyChat.get("permission"), true));

            int cooldown = intValue(legacyChat.get("cooldown"), 0);
            chat.put("cooldown", Math.max(cooldown, 0));

            Map<String, Object> spy = new LinkedHashMap<>();
            spy.put("enable", boolValue(legacyChat.get("spy"), true));
            String format = str(legacyChat.get("format"));
            String spyTemplate = legacySpyChatFormat != null ? legacySpyChatFormat : DEFAULT_SPY_FORMAT;
            spy.put("format", spyTemplate.replace("{format}", format != null ? format : DEFAULT_SPIED_FORMAT));
            chat.put("spy", spy);

            if (legacyChat.get("command") != null) {
                chat.put("command", str(legacyChat.get("command")));
            }
            if (legacyChat.get("aliases") instanceof List) {
                List<String> aliases = new ArrayList<>();
                for (Object alias : (List<?>) legacyChat.get("aliases")) {
                    if (alias != null) {
                        aliases.add(str(alias));
                    }
                }
                if (!aliases.isEmpty()) {
                    chat.put("aliases", aliases);
                }
            }
            if (legacyChat.get("moderation") != null) {
                notes.add("chat \"" + entry.getKey()
                        + "\": per-chat moderation toggles are not migrated.");
            }

            newChats.put(entry.getKey(), chat);
        }

        if (newChats.isEmpty()) {
            return;
        }

        editYaml(dataFolder.resolve("chats.yml"), root -> root.put("chats", newChats));
    }

    // ----- settings.yml ----------------------------------------------------

    private void migrateSettings(Map<String, Object> legacy, Path dataFolder) throws IOException {
        Map<String, Object> general = section(legacy, "general");
        Map<String, Object> mentions = section(legacy, "json", "mentions");
        if (general == null && mentions == null) {
            return;
        }

        editYaml(dataFolder.resolve("settings.yml"), root -> {
            if (general != null) {
                if (general.get("locale") != null) {
                    String legacyLocale = str(general.get("locale"));
                    String language = SettingsConfig.matchSupportedLanguage(legacyLocale);
                    if (language != null) {
                        root.put("language", language);
                    } else {
                        notes.add("locale \"" + legacyLocale + "\" has no v3 translation —"
                                + " settings.yml keeps the default language.");
                    }
                }
                Object priority = general.get("priority");
                if (priority != null) {
                    String name = str(priority).toUpperCase(Locale.ROOT);
                    if (VALID_PRIORITIES.contains(name)) {
                        root.put("listener-priority", name);
                    }
                }
                if (general.get("keep-old-recipients") != null) {
                    root.put("respect-foreign-recipients",
                            boolValue(general.get("keep-old-recipients"), true));
                }
                if (general.get("hide-vanished-recipients") != null) {
                    root.put("hide-vanished-recipients",
                            boolValue(general.get("hide-vanished-recipients"), false));
                }
            }
            if (mentions != null) {
                Map<String, Object> v3mentions = asMap(root.get("mentions"));
                if (v3mentions == null) {
                    v3mentions = new LinkedHashMap<>();
                    root.put("mentions", v3mentions);
                }
                if (mentions.get("enable") != null) {
                    v3mentions.put("enable", boolValue(mentions.get("enable"), true));
                }
                if (mentions.get("format") != null) {
                    // v2 mentions used {player}; v3 uses {username}.
                    v3mentions.put("target-format",
                            str(mentions.get("format")).replace("{player}", "{username}"));
                }
            }
        });
    }

    // ----- vanilla.yml -----------------------------------------------------

    private void migrateVanilla(Map<String, Object> legacy, Path dataFolder) throws IOException {
        Map<String, Object> vanilla = section(legacy, "miscellaneous", "vanilla");
        if (vanilla == null) {
            return;
        }

        editYaml(dataFolder.resolve("vanilla.yml"), root -> {
            migrateVanillaSection(asMap(vanilla.get("join")), asMap(root.get("join")));
            migrateVanillaSection(asMap(vanilla.get("quit")), asMap(root.get("quit")));
            migrateVanillaSection(asMap(vanilla.get("death")), asMap(root.get("death")));

            Map<String, Object> legacyJoin = asMap(vanilla.get("join"));
            Map<String, Object> v3join = asMap(root.get("join"));
            if (legacyJoin != null && v3join != null) {
                Map<String, Object> legacyFirstJoin = asMap(legacyJoin.get("first-join"));
                Map<String, Object> v3firstJoin = asMap(v3join.get("first-join"));
                if (legacyFirstJoin != null && v3firstJoin != null
                        && legacyFirstJoin.get("message") != null) {
                    v3firstJoin.put("message", str(legacyFirstJoin.get("message")));
                }
            }
        });
    }

    private void migrateVanillaSection(@Nullable Map<String, Object> legacy,
                                       @Nullable Map<String, Object> target) {
        if (legacy == null || target == null) {
            return;
        }
        if (legacy.get("enable") != null) {
            target.put("enable", boolValue(legacy.get("enable"), true));
        }
        if (legacy.get("message") != null) {
            target.put("message", str(legacy.get("message")));
        }
        if (legacy.get("permission") != null) {
            target.put("permission-required", boolValue(legacy.get("permission"), false));
        }
    }

    // ----- moderation.yml --------------------------------------------------

    private void migrateModeration(Map<String, Object> legacy, Path dataFolder) throws IOException {
        Map<String, Object> moderation = section(legacy, "moderation");
        if (moderation == null) {
            return;
        }

        editYaml(dataFolder.resolve("moderation.yml"), root -> {
            Map<String, Object> caps = asMap(moderation.get("caps"));
            Map<String, Object> v3caps = asMap(root.get("caps"));
            if (caps != null && v3caps != null) {
                copyBool(caps, "enable", v3caps, "enable");
                copyClampedInt(caps, "length", v3caps, "length", 1, Integer.MAX_VALUE, "caps.length");
                copyClampedInt(caps, "percent", v3caps, "percent", 0, 100, "caps.percent");
                copyBool(caps, "block", v3caps, "block");
            }

            Map<String, Object> ad = asMap(moderation.get("advertisement"));
            Map<String, Object> v3ad = asMap(root.get("advertisement"));
            if (ad != null && v3ad != null) {
                copyBool(ad, "enable", v3ad, "enable");
                copyBool(ad, "block", v3ad, "block");
                copyStr(ad, "replacement", v3ad, "replacement");
                Map<String, Object> patterns = asMap(ad.get("patterns"));
                if (patterns != null) {
                    if (patterns.get("ip") != null) {
                        v3ad.put("ip-pattern", str(patterns.get("ip")));
                    }
                    if (patterns.get("web") != null) {
                        v3ad.put("link-pattern", str(patterns.get("web")));
                    }
                }
                if (ad.get("whitelist") instanceof List) {
                    v3ad.put("whitelist", ad.get("whitelist"));
                }
            }

            Map<String, Object> swear = asMap(moderation.get("swear"));
            Map<String, Object> v3swear = asMap(root.get("swear"));
            if (swear != null && v3swear != null) {
                copyBool(swear, "enable", v3swear, "enable");
                copyBool(swear, "block", v3swear, "block");
                copyStr(swear, "replacement", v3swear, "replacement");
            }
        });
    }

    // ----- pm.yml ----------------------------------------------------------

    private void migratePm(Map<String, Object> legacy, Path dataFolder) throws IOException {
        Map<String, Object> pm = section(legacy, "pm");
        if (pm == null) {
            return;
        }

        editYaml(dataFolder.resolve("pm.yml"), root -> {
            copyBool(pm, "enable", root, "enable");
            copyBool(pm, "allow-console", root, "allow-console");
            Map<String, Object> format = asMap(pm.get("format"));
            if (format != null) {
                if (format.get("sender") != null) {
                    root.put("from-format", translatePmPlaceholders(str(format.get("sender"))));
                }
                if (format.get("recipient") != null) {
                    root.put("to-format", translatePmPlaceholders(str(format.get("recipient"))));
                }
            }
        });
        notes.add("PM spy format was not migrated — see pm.yml \"spy\" section.");
    }

    private static String translatePmPlaceholders(String format) {
        return format
                .replace("{sender-prefix}", "{from-prefix}")
                .replace("{sender-name}", "{from-name}")
                .replace("{sender-suffix}", "{from-suffix}")
                .replace("{recipient-prefix}", "{to-prefix}")
                .replace("{recipient-name}", "{to-name}")
                .replace("{recipient-suffix}", "{to-suffix}");
    }

    // ----- yaml helpers ----------------------------------------------------

    private void editYaml(Path file, Consumer<Map<String, Object>> editor) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        Map<String, Object> data;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            data = asMap(new Yaml().load(reader));
        }
        if (data == null) {
            data = new LinkedHashMap<>();
        }
        editor.accept(data);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            new Yaml(options).dump(data, writer);
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private static @Nullable Map<String, Object> section(Map<String, Object> root, String... path) {
        Map<String, Object> current = root;
        for (String key : path) {
            if (current == null) {
                return null;
            }
            current = asMap(current.get(key));
        }
        return current;
    }

    private static @Nullable String str(@Nullable Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static int intValue(@Nullable Object value, int fallback) {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private static boolean boolValue(@Nullable Object value, boolean fallback) {
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    private static void copyBool(Map<String, Object> from, String fromKey,
                                 Map<String, Object> to, String toKey) {
        if (from.get(fromKey) instanceof Boolean) {
            to.put(toKey, from.get(fromKey));
        }
    }

    private void copyClampedInt(Map<String, Object> from, String fromKey,
                                Map<String, Object> to, String toKey,
                                int min, int max, String label) {
        if (!(from.get(fromKey) instanceof Number)) {
            return;
        }
        int value = ((Number) from.get(fromKey)).intValue();
        int clamped = Math.min(Math.max(value, min), max);
        if (clamped != value) {
            notes.add(label + " " + value + " is outside the range v3 accepts"
                    + " and was clamped to " + clamped + ".");
        }
        to.put(toKey, clamped);
    }

    private static void copyStr(Map<String, Object> from, String fromKey,
                                Map<String, Object> to, String toKey) {
        if (from.get(fromKey) != null) {
            to.put(toKey, str(from.get(fromKey)));
        }
    }

}

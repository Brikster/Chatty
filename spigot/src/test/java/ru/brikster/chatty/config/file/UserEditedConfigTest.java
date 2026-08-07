package ru.brikster.chatty.config.file;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.brikster.chatty.config.serdes.SerdesChatty;
import ru.brikster.chatty.convert.component.InternalMiniMessageStringConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserEditedConfigTest {

    @TempDir
    Path dataFolder;

    @Test
    void reloadsAfterMessagesAreAddedToANotificationList() throws IOException {
        Path file = dataFolder.resolve("notifications.yml");

        create(NotificationsConfig.class, file);

        List<String> lines = Files.readAllLines(file);
        int anchor = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("Second message from default channel")) {
                anchor = i + 1;
                break;
            }
        }
        assertTrue(anchor > 0, "could not find the generated default messages to extend");
        lines.add(anchor + 1, "      - '&aFourth message added by the server owner'");
        lines.add(anchor + 1, "      - '&aThird message added by the server owner'");
        Files.write(file, lines);

        assertDoesNotThrow(() -> create(NotificationsConfig.class, file),
                "the plugin cannot reload notifications.yml after an admin added messages");

        String reloaded = Files.readString(file);
        assertTrue(reloaded.contains("Third message added by the server owner"),
                "the added message was dropped on reload");
        assertTrue(reloaded.contains("Fourth message added by the server owner"),
                "the added message was dropped on reload");
    }

    @Test
    void reloadsAfterAWholeNotificationListIsAdded() throws IOException {
        Path file = dataFolder.resolve("notifications.yml");

        create(NotificationsConfig.class, file);

        List<String> lines = Files.readAllLines(file);
        int anchor = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("    default:")) {
                anchor = i;
                break;
            }
        }
        assertTrue(anchor >= 0, "could not find the generated default list");
        lines.addAll(anchor, List.of(
                "    rules:",
                "      period: 120",
                "      messages:",
                "      - '&cRule 1: be nice'",
                "      - '&cRule 2: no spam'",
                "      play-sound: true",
                "      sound:",
                "        name: minecraft:block.note_block.pling",
                "        source: MASTER",
                "        volume: 1.0",
                "        pitch: 1.0",
                "      permission-required: false",
                "      random-order: false"));
        Files.write(file, lines);

        assertDoesNotThrow(() -> create(NotificationsConfig.class, file),
                "the plugin cannot reload notifications.yml after an admin added a notification list");

        String reloaded = Files.readString(file);
        assertTrue(reloaded.contains("Rule 1: be nice"), "the added list was dropped on reload");
        assertTrue(reloaded.contains("block.note_block.pling"), "the added list's sound was dropped on reload");
    }

    private void create(Class<? extends OkaeriConfig> configClass, Path file) {
        InternalMiniMessageStringConverter converter = new InternalMiniMessageStringConverter();
        try {
            configClass.getDeclaredField("converter").set(null, converter);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot inject converter into " + configClass.getSimpleName(), e);
        }

        ConfigManager.create(configClass, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlSnakeYamlConfigurer(), true),
                    new SerdesCommons(), new SerdesChatty(converter));
            config.withBindFile(file);
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });
    }

}

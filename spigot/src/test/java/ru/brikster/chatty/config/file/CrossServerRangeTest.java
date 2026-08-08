package ru.brikster.chatty.config.file;

import eu.okaeri.configs.ConfigManager;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CrossServerRangeTest {

    @TempDir
    Path dataFolder;

    @Test
    void loadsAChatConfiguredForCrossServerChat() throws IOException {
        Path file = dataFolder.resolve("chats.yml");
        Files.writeString(file, String.join("\n",
                "chats:",
                "  global:",
                "    format: '{player}: {message}'",
                "    message-format: '{original-message}'",
                "    symbol: ''",
                "    range: -3",
                "    permission-required: false"));

        ChatsConfig config = assertDoesNotThrow(() -> load(file),
                "range -3 is the cross-server switch and must be a valid value");

        assertEquals(-3, config.getChats().get("global").getRange());
    }

    private ChatsConfig load(Path file) {
        InternalMiniMessageStringConverter converter = new InternalMiniMessageStringConverter();
        return ConfigManager.create(ChatsConfig.class, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlSnakeYamlConfigurer(), true),
                    new SerdesCommons(), new SerdesChatty(converter));
            config.withBindFile(file);
            config.withRemoveOrphans(true);
            config.load(true);
        });
    }

}

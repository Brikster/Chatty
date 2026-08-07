package ru.brikster.chatty.config.file;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.brikster.chatty.config.serdes.SerdesChatty;
import ru.brikster.chatty.convert.component.InternalMiniMessageStringConverter;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConfigGenerationTest {

    @TempDir
    Path dataFolder;

    @ParameterizedTest
    @ValueSource(classes = {
            SettingsConfig.class,
            ChatsConfig.class,
            PmConfig.class,
            MessagesConfig.class,
            VanillaConfig.class,
            ModerationConfig.class,
            NotificationsConfig.class,
            ReplacementsConfig.class,
            ProxyConfig.class,
    })
    void writesDefaultsWithoutError(Class<? extends OkaeriConfig> configClass) {
        InternalMiniMessageStringConverter converter = new InternalMiniMessageStringConverter();
        try {
            configClass.getDeclaredField("converter").set(null, converter);
        } catch (NoSuchFieldException ignored) {
            // Not every config renders components.
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot inject converter into " + configClass.getSimpleName(), e);
        }

        Path file = dataFolder.resolve(configClass.getSimpleName() + ".yml");

        assertDoesNotThrow(() -> ConfigManager.create(configClass, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlSnakeYamlConfigurer(), true),
                    new SerdesCommons(), new SerdesChatty(converter));
            config.withBindFile(file);
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        }), configClass.getSimpleName() + " cannot be generated");
    }

}

package ru.brikster.chatty.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.Plugin;
import ru.brikster.chatty.chat.component.impl.DummyPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructor;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructorImpl;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.registry.MemoryChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.chat.selection.ChatSelectorImpl;
import ru.brikster.chatty.config.serdes.SerdesChatty;
import ru.brikster.chatty.config.type.*;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.convert.component.MiniMessageStringConverter;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;
import ru.brikster.chatty.prefix.NullPrefixProvider;
import ru.brikster.chatty.prefix.PrefixProvider;

import java.nio.file.Path;

public final class GeneralGuiceModule extends AbstractModule {

    private final Plugin plugin;
    private final BukkitAudiences audienceProvider;

    private final Path dataFolderPath;

    private final ChatRegistry chatRegistry;

    private final SerdesChatty serdesChatty;

    public GeneralGuiceModule(final Plugin plugin,
                              final BukkitAudiences audienceProvider,
                              final Path dataFolderPath) {
        this.plugin = plugin;
        this.audienceProvider = audienceProvider;
        this.dataFolderPath = dataFolderPath;

        this.chatRegistry = new MemoryChatRegistry();
        this.serdesChatty = new SerdesChatty(MiniMessageStringConverter.miniMessageStringConverter());
    }

    @Override
    protected void configure() {
        bind(Plugin.class).toInstance(plugin);
        bind(ChatRegistry.class).toInstance(chatRegistry);

        bind(ComponentStringConverter.class).toInstance(MiniMessageStringConverter.miniMessageStringConverter());
        bind(MessageConverter.class).toInstance(new LegacyToMiniMessageConverter());
        bind(ComponentFromContextConstructor.class).toInstance(new ComponentFromContextConstructorImpl());
        bind(PrefixProvider.class).toInstance(new NullPrefixProvider());
        bind(ChatSelector.class).toInstance(new ChatSelectorImpl());

        bind(BukkitAudiences.class).toInstance(audienceProvider);

        bind(SettingsConfig.class).toInstance(createConfig(SettingsConfig.class, "settings.yml"));
        bind(ChatsConfig.class).toInstance(createConfig(ChatsConfig.class, "chats.yml"));
        bind(MessagesConfig.class).toInstance(createConfig(MessagesConfig.class, "messages.yml"));
        bind(VanillaConfig.class).toInstance(createConfig(VanillaConfig.class, "vanilla.yml"));
        bind(ModerationConfig.class).toInstance(createConfig(ModerationConfig.class, "moderation.yml"));
        bind(NotificationsConfig.class).toInstance(createConfig(NotificationsConfig.class, "notifications.yml"));
    }

    @Provides
    public PlaceholdersComponentTransformer placeholdersComponentTransformer() {
        return new DummyPlaceholdersComponentTransformer();
    }

    private <T extends OkaeriConfig> T createConfig(Class<T> configClass, String fileName) {
        try {
            configClass.getDeclaredField("converter").set(null, MiniMessageStringConverter.miniMessageStringConverter());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot inject converter into " + configClass.getSimpleName() + " class", e);
        } catch (NoSuchFieldException ignored) {}

        return ConfigManager.create(configClass, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlSnakeYamlConfigurer(), true), new SerdesCommons(), new SerdesBukkit(), serdesChatty);
            config.withBindFile(dataFolderPath.resolve(fileName));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });
    }

}

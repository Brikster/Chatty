package ru.brikster.chatty.guice;

import com.google.inject.AbstractModule;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.Plugin;
import ru.brikster.chatty.chat.construct.MessageConstructor;
import ru.brikster.chatty.chat.construct.MessageConstructorImpl;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.registry.MemoryChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.chat.selection.ChatSelectorImpl;
import ru.brikster.chatty.config.serdes.SerdesChatty;
import ru.brikster.chatty.config.type.*;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.convert.component.MiniMessageConverter;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;
import ru.brikster.chatty.prefix.DefaultPrefixProvider;
import ru.brikster.chatty.prefix.PrefixProvider;

import lombok.Getter;

import java.nio.file.Path;

public class ChattyGuiceModule extends AbstractModule {

    private final Plugin plugin;

    private final BukkitAudiences audienceProvider;
    private final Path dataFolderPath;

    private final @Getter ChatRegistry chatRegistry;

    private final @Getter ComponentConverter converter;

    private final @Getter SettingsConfig settingsConfig;
    private final @Getter ChatsConfig chatsConfig;
    private final MessagesConfig messagesConfig;
    private final VanillaConfig vanillaConfig;
    private final ModerationConfig moderation;
    private final @Getter NotificationsConfig notifications;

    public ChattyGuiceModule(final Plugin plugin,
                             final BukkitAudiences audienceProvider,
                             final Path dataFolderPath) {
        this.plugin = plugin;
        this.audienceProvider = audienceProvider;
        this.dataFolderPath = dataFolderPath;

        this.chatRegistry = new MemoryChatRegistry();

        this.converter = new MiniMessageConverter();

        SerdesChatty serdesChatty = new SerdesChatty(converter);
        this.settingsConfig = initConfig(SettingsConfig.class, "settings.yml", serdesChatty);
        this.chatsConfig = initConfig(ChatsConfig.class, "chats.yml", serdesChatty);
        this.messagesConfig = initConfig(MessagesConfig.class, "messages.yml", serdesChatty);
        this.vanillaConfig = initConfig(VanillaConfig.class, "vanilla.yml", serdesChatty);
        this.moderation = initConfig(ModerationConfig.class, "moderation.yml", serdesChatty);
        this.notifications = initConfig(NotificationsConfig.class, "notifications.yml", serdesChatty);
    }

    @Override
    protected void configure() {
        bind(BukkitAudiences.class).toInstance(audienceProvider);

        bind(SettingsConfig.class).toInstance(settingsConfig);
        bind(ChatsConfig.class).toInstance(chatsConfig);
        bind(MessagesConfig.class).toInstance(messagesConfig);
        bind(VanillaConfig.class).toInstance(vanillaConfig);

        bind(Plugin.class).toInstance(plugin);
//        bind(Logger.class).toInstance(plugin.getLogger());
        bind(ChatRegistry.class).toInstance(chatRegistry);

        bind(ComponentConverter.class).toInstance(converter);
        bind(MessageConverter.class).toInstance(new LegacyToMiniMessageConverter());
        bind(MessageConstructor.class).toInstance(new MessageConstructorImpl());
        bind(PrefixProvider.class).toInstance(new DefaultPrefixProvider());
        bind(ChatSelector.class).toInstance(new ChatSelectorImpl());
    }

    private <T extends OkaeriConfig> T initConfig(Class<T> configClass, String fileName, final SerdesChatty serdesChatty) {
        return ConfigManager.create(configClass, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlSnakeYamlConfigurer(), true), serdesChatty, new SerdesCommons(), new SerdesBukkit());
            config.withBindFile(dataFolderPath.resolve(fileName));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });
    }

}

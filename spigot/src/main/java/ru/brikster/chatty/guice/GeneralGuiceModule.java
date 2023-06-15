package ru.brikster.chatty.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.component.impl.DummyPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructor;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructorImpl;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessor;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessorImpl;
import ru.brikster.chatty.chat.message.transform.stage.early.range.RangeLimiterMessageTransformStrategy;
import ru.brikster.chatty.chat.message.transform.stage.early.symbol.RemoveChatSymbolMessageTransformStrategy;
import ru.brikster.chatty.chat.message.transform.stage.intermediary.IntermediateMessageTransformer;
import ru.brikster.chatty.chat.message.transform.stage.intermediary.IntermediateMessageTransformerImpl;
import ru.brikster.chatty.chat.message.transform.stage.late.papi.PlaceholdersMessageTransformStrategy;
import ru.brikster.chatty.chat.message.transform.stage.late.prefix.PrefixMessageTransformStrategy;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.registry.MemoryChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.chat.selection.ChatSelectorImpl;
import ru.brikster.chatty.config.serdes.SerdesChatty;
import ru.brikster.chatty.config.type.*;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.convert.component.SystemMiniMessageStringConverter;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;
import ru.brikster.chatty.notification.NotificationTicker;
import ru.brikster.chatty.notification.ScheduledExecutorNotificationTicker;
import ru.brikster.chatty.prefix.NullPrefixProvider;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.brikster.chatty.prefix.VaultPrefixProvider;

import java.nio.file.Path;

public final class GeneralGuiceModule extends AbstractModule {

    private final Plugin plugin;
    private final BukkitAudiences audienceProvider;

    private final Path dataFolderPath;

    private final SystemMiniMessageStringConverter systemMiniMessageStringConverter;

    private final ChatRegistry chatRegistry;
    private final SerdesChatty serdesChatty;

    public GeneralGuiceModule(final Plugin plugin,
                              final BukkitAudiences audienceProvider,
                              final Path dataFolderPath) {
        this.plugin = plugin;
        this.audienceProvider = audienceProvider;
        this.dataFolderPath = dataFolderPath;

        this.systemMiniMessageStringConverter = new SystemMiniMessageStringConverter();
        this.chatRegistry = new MemoryChatRegistry();
        this.serdesChatty = new SerdesChatty(systemMiniMessageStringConverter);
    }

    @Override
    protected void configure() {
        bind(Plugin.class).toInstance(plugin);
        bind(ChatRegistry.class).toInstance(chatRegistry);

        bind(MessageTransformStrategiesProcessor.class).to(MessageTransformStrategiesProcessorImpl.class);
        bind(ComponentStringConverter.class).toInstance(systemMiniMessageStringConverter);
        bind(MessageConverter.class).toInstance(new LegacyToMiniMessageConverter());
        bind(ComponentFromContextConstructor.class).toInstance(new ComponentFromContextConstructorImpl());

        bind(ChatSelector.class).toInstance(new ChatSelectorImpl());
        bind(NotificationTicker.class).toInstance(new ScheduledExecutorNotificationTicker());

        bind(BukkitAudiences.class).toInstance(audienceProvider);

        bind(SettingsConfig.class).toInstance(createConfig(SettingsConfig.class, "settings.yml"));
        bind(ChatsConfig.class).toInstance(createConfig(ChatsConfig.class, "chats.yml"));
        bind(MessagesConfig.class).toInstance(createConfig(MessagesConfig.class, "messages.yml"));
        bind(VanillaConfig.class).toInstance(createConfig(VanillaConfig.class, "vanilla.yml"));
        bind(ModerationConfig.class).toInstance(createConfig(ModerationConfig.class, "moderation.yml"));
        bind(NotificationsConfig.class).toInstance(createConfig(NotificationsConfig.class, "notifications.yml"));

        Multibinder<MessageTransformStrategy<?>> strategyMultibinder = Multibinder.newSetBinder(binder(), new TypeLiteral<MessageTransformStrategy<?>>() {});
        // Early
        strategyMultibinder.addBinding().to(RemoveChatSymbolMessageTransformStrategy.class);
        strategyMultibinder.addBinding().to(RangeLimiterMessageTransformStrategy.class);
        // Late
        strategyMultibinder.addBinding().to(PlaceholdersMessageTransformStrategy.class);
        strategyMultibinder.addBinding().to(PrefixMessageTransformStrategy.class);

        bind(IntermediateMessageTransformer.class).to(IntermediateMessageTransformerImpl.class);
    }

    @Provides
    public PrefixProvider prefixProvider() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault")
                ? new VaultPrefixProvider()
                : new NullPrefixProvider();
    }

    @Provides
    public PlaceholdersComponentTransformer placeholdersComponentTransformer() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? new PlaceholderApiComponentTransformer()
                : new DummyPlaceholdersComponentTransformer();
    }

    @SuppressWarnings("VulnerableCodeUsages")
    private <ConfigT extends OkaeriConfig> ConfigT createConfig(Class<ConfigT> configClass, String fileName) {
        try {
            configClass.getDeclaredField("converter").set(null, systemMiniMessageStringConverter);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot inject converter into " + configClass.getSimpleName() + " class", e);
        } catch (NoSuchFieldException ignored) {}

        return ConfigManager.create(configClass, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlSnakeYamlConfigurer(new Yaml(
                    new Constructor(),
                    new Representer() {
                        {
                            setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                        }
                    },
                    new DumperOptions() {
                        {
                            setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                            setSplitLines(false);
                        }
                    },
                    new LoaderOptions(),
                    new Resolver())), true),
                    new SerdesCommons(), new SerdesBukkit(), serdesChatty);
            config.withBindFile(dataFolderPath.resolve(fileName));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });
    }

}

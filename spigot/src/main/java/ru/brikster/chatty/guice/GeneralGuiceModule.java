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
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.dummy.DummyPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.dummy.DummyRelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.papi.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.chat.component.impl.papi.PlaceholderApiRelationalComponentTransformer;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructor;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructorImpl;
import ru.brikster.chatty.chat.message.transform.intermediary.IntermediateMessageTransformer;
import ru.brikster.chatty.chat.message.transform.intermediary.IntermediateMessageTransformerImpl;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessor;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessorImpl;
import ru.brikster.chatty.chat.message.transform.stage.early.RangeLimiterStrategy;
import ru.brikster.chatty.chat.message.transform.stage.early.RemoveChatSymbolStrategy;
import ru.brikster.chatty.chat.message.transform.stage.late.papi.PlaceholdersStrategy;
import ru.brikster.chatty.chat.message.transform.stage.late.prefix.PrefixStrategy;
import ru.brikster.chatty.chat.message.transform.stage.middle.LinkParserTransformStrategy;
import ru.brikster.chatty.chat.message.transform.stage.post.RelationalPlaceholdersStrategy;
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
import ru.brikster.chatty.prefix.LuckpermsPrefixProvider;
import ru.brikster.chatty.prefix.NullPrefixProvider;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.brikster.chatty.prefix.VaultPrefixProvider;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.regex.Pattern;

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
        bind(MessageConverter.class).to(LegacyToMiniMessageConverter.class);
        bind(ComponentFromContextConstructor.class).to(ComponentFromContextConstructorImpl.class);

        bind(ChatSelector.class).to(ChatSelectorImpl.class);
        bind(NotificationTicker.class).to(ScheduledExecutorNotificationTicker.class);

        bind(BukkitAudiences.class).toInstance(audienceProvider);

        bind(SettingsConfig.class).toInstance(createConfig(SettingsConfig.class, "settings.yml"));
        bind(ChatsConfig.class).toInstance(createConfig(ChatsConfig.class, "chats.yml"));
        bind(MessagesConfig.class).toInstance(createConfig(MessagesConfig.class, "messages.yml"));
        bind(VanillaConfig.class).toInstance(createConfig(VanillaConfig.class, "vanilla.yml"));
        bind(ModerationConfig.class).toInstance(createConfig(ModerationConfig.class, "moderation.yml"));
        bind(NotificationsConfig.class).toInstance(createConfig(NotificationsConfig.class, "notifications.yml"));

        Multibinder<MessageTransformStrategy<?>> strategyMultibinder = Multibinder.newSetBinder(binder(), new TypeLiteral<MessageTransformStrategy<?>>() {});
        // Early
        strategyMultibinder.addBinding().to(RemoveChatSymbolStrategy.class);
        strategyMultibinder.addBinding().to(RangeLimiterStrategy.class);
        // Middle
        strategyMultibinder.addBinding().to(LinkParserTransformStrategy.class);
        // Late
        strategyMultibinder.addBinding().to(PlaceholdersStrategy.class);
        strategyMultibinder.addBinding().to(PrefixStrategy.class);
        // Post
        strategyMultibinder.addBinding().to(RelationalPlaceholdersStrategy.class);

        bind(IntermediateMessageTransformer.class).to(IntermediateMessageTransformerImpl.class);
    }

    @Provides
    @Singleton
    public PrefixProvider prefixProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            plugin.getLogger().log(Level.INFO, "Using LuckPerms as prefix provider");
            return new LuckpermsPrefixProvider();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            plugin.getLogger().log(Level.INFO, "Using Vault as prefix provider");
            return new VaultPrefixProvider();
        }
        plugin.getLogger().log(Level.WARNING, "Cannot find corresponding prefix provider, try to install Vault to take prefixes from your permission management plugin");
        return new NullPrefixProvider();
    }

    @Provides
    @Singleton
    public PlaceholdersComponentTransformer placeholdersComponentTransformer(ComponentStringConverter componentStringConverter) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? new PlaceholderApiComponentTransformer(componentStringConverter)
                : new DummyPlaceholdersComponentTransformer();
    }

    @Provides
    @Singleton
    public RelationalPlaceholdersComponentTransformer relationalPlaceholdersComponentTransformer(ComponentStringConverter componentStringConverter) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? new PlaceholderApiRelationalComponentTransformer(componentStringConverter)
                : new DummyRelationalPlaceholdersComponentTransformer();
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
                            this.representers.put(String.class, new RepresentString() {

                                private final Pattern MULTILINE_PATTERN = Pattern.compile("[\n\u0085\u2028\u2029]");

                                @Override
                                public Node representData(Object data) {
                                    Tag tag = Tag.STR;
                                    DumperOptions.ScalarStyle style = data instanceof String && ((String) data).contains("'")
                                            ? ScalarStyle.DOUBLE_QUOTED // Prevent double-quoting single quotes of MiniMessage format
                                            : null; // not defined
                                    String value = data.toString();
                                    if (nonPrintableStyle == DumperOptions.NonPrintableStyle.BINARY && !StreamReader.isPrintable(value)) {
                                        tag = Tag.BINARY;
                                        char[] binary;
                                        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                                        // sometimes above will just silently fail - it will return incomplete data
                                        // it happens when String has invalid code points
                                        // (for example half surrogate character without other half)
                                        final String checkValue = new String(bytes, StandardCharsets.UTF_8);
                                        if (!checkValue.equals(value)) {
                                            throw new YAMLException("invalid string value has occurred");
                                        }
                                        binary = Base64Coder.encode(bytes);
                                        value = String.valueOf(binary);
                                        style = DumperOptions.ScalarStyle.LITERAL;
                                    }
                                    // if no other scalar style is explicitly set, use literal style for
                                    // multiline scalars
                                    if (MULTILINE_PATTERN.matcher(value).find()) {
                                        style = DumperOptions.ScalarStyle.LITERAL;
                                    }
                                    return representScalar(tag, value, style);
                                }
                            });
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

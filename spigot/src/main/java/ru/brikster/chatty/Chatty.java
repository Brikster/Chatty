package ru.brikster.chatty;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.message.strategy.impl.ConvertComponentMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.RemoveChatSymbolMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.papi.PlaceholderApiMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.vault.PrefixMessageTransformStrategy;
import ru.brikster.chatty.config.object.ChatConfigDeclaration;
import ru.brikster.chatty.config.type.ExampleConfig;
import ru.brikster.chatty.config.type.NotificationsConfig.ActionbarNotificationsConfig.ActionbarNotificationChannelConfig;
import ru.brikster.chatty.config.type.NotificationsConfig.ChatNotificationsConfig.ChatNotificationChannelConfig;
import ru.brikster.chatty.config.type.NotificationsConfig.TitleNotificationsConfig.TitleNotificationChannelConfig;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.guice.ChattyGuiceModule;
import ru.brikster.chatty.misc.VanillaListener;
import ru.brikster.chatty.notification.ActionbarNotification;
import ru.brikster.chatty.notification.ChatNotification;
import ru.brikster.chatty.notification.TitleNotification;
import ru.brikster.chatty.util.Pair;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;

import lombok.SneakyThrows;

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class Chatty extends BukkitBasePlugin {

    private static Chatty instance;

    @SneakyThrows
    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        ChattyInitEvent initEvent = new ChattyInitEvent(BukkitAudiences.create(this));
        getServer().getPluginManager().callEvent(initEvent);

        ChattyGuiceModule guiceModule = new ChattyGuiceModule(
                Chatty.this,
                initEvent.getAudienceProvider(),
                getDataFolder().toPath());

        Injector injector = Guice.createInjector(guiceModule);

        injector.injectMembers(ConvertComponentMessageTransformStrategy.instance());
        injector.injectMembers(PrefixMessageTransformStrategy.instance());

        for (Entry<String, ChatConfigDeclaration> entry : guiceModule.getChatsConfig().getChats().entrySet()) {
            ChatConfigDeclaration declaration = entry.getValue();
            Chat chat = new ChatImpl(entry.getKey(),
                    declaration.getDisplayName(), guiceModule.getConverter().convert(declaration.getFormat()),
                    declaration.getSymbol(), null, declaration.getRange(), false);

            chat.addStrategy(RemoveChatSymbolMessageTransformStrategy.instance());
            chat.addStrategy(ConvertComponentMessageTransformStrategy.instance());
            chat.addStrategy(PrefixMessageTransformStrategy.instance());
            chat.addStrategy(PlaceholderApiMessageTransformStrategy.instance());

            guiceModule.getChatRegistry().register(chat);
        }

        ComponentConverter componentConverter = guiceModule.getConverter();

        if (guiceModule.getNotifications().getChat().isEnable()) {
            for (Entry<String, ChatNotificationChannelConfig> entry : guiceModule.getNotifications().getChat().getChannels().entrySet()) {
                ChatNotificationChannelConfig config = entry.getValue();
                ChatNotification chatNotification = ChatNotification.create(
                        entry.getKey(), config.getPeriod(),
                        config.getMessages().stream().map(componentConverter::convert).collect(Collectors.toList()),
                        config.isPermissionRequired(), config.isRandomOrder());
                injector.injectMembers(chatNotification);
                chatNotification.schedule();
            }
        }

        if (guiceModule.getNotifications().getTitle().isEnable()) {
            for (Entry<String, TitleNotificationChannelConfig> entry : guiceModule.getNotifications().getTitle().getChannels().entrySet()) {
                TitleNotificationChannelConfig config = entry.getValue();
                TitleNotification chatNotification = TitleNotification.create(
                        entry.getKey(), config.getPeriod(),
                        config.getMessages()
                                .stream()
                                .map($ -> Pair.create(componentConverter.convert($.getTitle()), componentConverter.convert($.getSubtitle())))
                                .collect(Collectors.toList()),
                        config.isPermissionRequired(), config.isRandomOrder());
                injector.injectMembers(chatNotification);
                chatNotification.schedule();
            }
        }

        if (guiceModule.getNotifications().getActionbar().isEnable()) {
            for (Entry<String, ActionbarNotificationChannelConfig> entry : guiceModule.getNotifications().getActionbar().getChannels().entrySet()) {
                ActionbarNotificationChannelConfig config = entry.getValue();
                ActionbarNotification chatNotification = ActionbarNotification.create(
                        entry.getKey(), config.getPeriod(), config.getStay(),
                        config.getMessages()
                                .stream()
                                .map(componentConverter::convert)
                                .collect(Collectors.toList()),
                        config.isPermissionRequired(), config.isRandomOrder());
                injector.injectMembers(chatNotification);
                chatNotification.schedule();
            }
        }

        ConfigManager.create(ExampleConfig.class, config -> {
            config.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesCommons(), new SerdesBukkit());
            config.withBindFile(getDataFolder().toPath().resolve("example.yml"));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });

        EventPriority priority = guiceModule.getSettingsConfig().getListenerPriority();
        if (priority == EventPriority.MONITOR) {
            priority = EventPriority.HIGHEST;
            getLogger().log(Level.WARNING, "Cannot use monitor priority for listener");
        }

        LegacyEventExecutor chatListener = injector.getInstance(LegacyEventExecutor.class);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, chatListener, Chatty.instance, true);

        VanillaListener miscListener = injector.getInstance(VanillaListener.class);
        this.getServer().getPluginManager().registerEvents(miscListener, this);

//        if (config.getNode("general.bungeecord").getAsBoolean(false)) {
//            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
//            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener(getExact(ChatManager.class)));
//        }

//        Chatty.api = new ChattyApiImpl(getExact(ChatManager.class).getChats().stream().filter(Chat::isEnable).collect(Collectors.toSet()));
//        ChattyApiHolder.setApi(api);

//        MetricsUtil.run();
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
//        this.getExact(CommandManager.class).unregisterAll();
//        this.getExact(ChatManager.class).getChats().forEach(chat -> {
//            if (chat.getBukkitCommand() != null) {
//                chat.getBukkitCommand().unregister(Chatty.get());
//            }
//        });
    }

}

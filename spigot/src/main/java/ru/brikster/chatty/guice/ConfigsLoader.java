package ru.brikster.chatty.guice;

import com.google.inject.Inject;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.message.strategy.impl.ConvertComponentMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.RemoveChatSymbolMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.papi.PlaceholderApiMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.range.RangeLimiterMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.vault.PrefixMessageTransformStrategy;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.config.object.ChatProperties;
import ru.brikster.chatty.config.type.ChatsConfig;
import ru.brikster.chatty.config.type.NotificationsConfig;
import ru.brikster.chatty.config.type.NotificationsConfig.ActionbarNotificationsConfig.ActionbarNotificationChannelConfig;
import ru.brikster.chatty.config.type.NotificationsConfig.ChatNotificationsConfig.ChatNotificationChannelConfig;
import ru.brikster.chatty.config.type.NotificationsConfig.TitleNotificationsConfig.TitleNotificationChannelConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.notification.ActionbarNotification;
import ru.brikster.chatty.notification.ChatNotification;
import ru.brikster.chatty.notification.NotificationTicker;
import ru.brikster.chatty.notification.TitleNotification;
import ru.brikster.chatty.notification.TitleNotification.TitleNotificationMessage;

import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class ConfigsLoader {

    @Inject
    public void loadChannels(ChatsConfig config,
                             ChatRegistry registry,
                             ComponentStringConverter componentConverter) {
        for (Entry<String, ChatProperties> entry : config.getChats().entrySet()) {
            ChatProperties declaration = entry.getValue();
            Chat chat = new ChatImpl(entry.getKey(),
                    declaration.getDisplayName(), componentConverter.stringToComponent(declaration.getFormat()),
                    declaration.getSymbol(), null, declaration.getRange(), false);

            chat.addStrategy(RemoveChatSymbolMessageTransformStrategy.instance());
            chat.addStrategy(RangeLimiterMessageTransformStrategy.instance());
            chat.addStrategy(ConvertComponentMessageTransformStrategy.instance());
            chat.addStrategy(PrefixMessageTransformStrategy.instance());

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                chat.addStrategy(PlaceholderApiMessageTransformStrategy.instance());
            }

            registry.register(chat);
        }
    }

    @Inject
    public void loadTitleNotifications(NotificationTicker ticker,
                                       NotificationsConfig notifications,
                                       BukkitAudiences audiences,
                                       PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        if (notifications.getTitle().isEnable()) {
            for (Entry<String, TitleNotificationChannelConfig> entry : notifications.getTitle().getLists().entrySet()) {
                TitleNotificationChannelConfig config = entry.getValue();
                TitleNotification titleNotification = new TitleNotification(
                        entry.getKey(), config.getPeriod(),
                        config.getMessages()
                                .stream()
                                .map(titleConfig -> new TitleNotificationMessage(
                                        titleConfig.getTitle(),
                                        titleConfig.getSubtitle()))
                                .collect(Collectors.toList()),
                        config.isPermissionRequired(),
                        config.isRandomOrder(),
                        audiences,
                        placeholdersComponentTransformer);
                ticker.addNotification(titleNotification);
            }
        }
    }

    @Inject
    public void loadChatNotifications(NotificationTicker ticker,
                                      NotificationsConfig notifications,
                                      BukkitAudiences audiences,
                                      PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        if (notifications.getChat().isEnable()) {
            for (Entry<String, ChatNotificationChannelConfig> entry : notifications.getChat().getLists().entrySet()) {
                ChatNotificationChannelConfig config = entry.getValue();
                ChatNotification chatNotification = new ChatNotification(
                        entry.getKey(), config.getPeriod(),
                        config.getMessages(),
                        config.isPermissionRequired(), config.isRandomOrder(),
                        audiences,
                        placeholdersComponentTransformer);
                ticker.addNotification(chatNotification);
            }
        }
    }

    @Inject
    public void loadActionbarNotifications(NotificationTicker ticker,
                                           NotificationsConfig notifications,
                                           BukkitAudiences audiences,
                                           PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        if (notifications.getActionbar().isEnable()) {
            for (Entry<String, ActionbarNotificationChannelConfig> entry : notifications.getActionbar()
                    .getLists().entrySet()) {
                ActionbarNotificationChannelConfig config = entry.getValue();
                ActionbarNotification actionbarNotification = new ActionbarNotification(
                        entry.getKey(), config.getPeriod(), config.getStay(),
                        config.getMessages(),
                        config.isPermissionRequired(), config.isRandomOrder(),
                        audiences,
                        placeholdersComponentTransformer);
                ticker.addNotification(actionbarNotification);
            }
        }
    }

}

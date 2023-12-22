package ru.brikster.chatty.guice;

import com.google.inject.Inject;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.ChatStyle;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.config.file.ChatsConfig;
import ru.brikster.chatty.config.file.NotificationsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.notification.ActionbarNotification;
import ru.brikster.chatty.notification.ChatNotification;
import ru.brikster.chatty.notification.NotificationTicker;
import ru.brikster.chatty.notification.TitleNotification;
import ru.brikster.chatty.notification.TitleNotification.TitleNotificationMessage;

import java.util.stream.Collectors;

public final class ConfigsLoader {

    @Inject
    public void loadChannels(ChatsConfig config,
                             ChatRegistry registry,
                             ComponentStringConverter componentConverter) {
        config.getChats().forEach((chatId, chatConfig) -> {
            Chat chat = new ChatImpl(chatId,
                    chatConfig.getDisplayName(), componentConverter.stringToComponent(chatConfig.getFormat()),
                    chatConfig.getSymbol(), null, chatConfig.getRange(), chatConfig.isPermissionRequired(),
                    chatConfig
                            .getStyles()
                            .entrySet()
                            .stream()
                            .map(styleEntry -> new ChatStyle(styleEntry.getKey(),
                                    componentConverter.stringToComponent(styleEntry.getValue().getFormat()),
                                    styleEntry.getValue().getPriority()))
                            .collect(Collectors.toSet()),
                    chatConfig.isNotifyNobodyHeard(),
                    chatConfig.isParseLinks(),
                    chatConfig.getSpy().isEnable(),
                    chatConfig.isPlaySound() ? chatConfig.getSound() : null,
                    componentConverter.stringToComponent(chatConfig.getSpy().getFormat()),
                    chatConfig.getCooldown());
            registry.register(chatId, chat);
        });
    }

    @Inject
    public void loadTitleNotifications(NotificationTicker ticker,
                                       NotificationsConfig config,
                                       BukkitAudiences audiences,
                                       PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        if (config.getTitle().isEnable()) {
            config.getTitle().getLists().forEach((channelId, channelConfig) -> {
                TitleNotification titleNotification = new TitleNotification(
                        channelId, channelConfig.getPeriod(),
                        channelConfig.getMessages()
                                .stream()
                                .map(titleConfig -> new TitleNotificationMessage(
                                        titleConfig.getTitle(),
                                        titleConfig.getSubtitle()))
                                .collect(Collectors.toList()),
                        channelConfig.isPermissionRequired(),
                        channelConfig.isRandomOrder(),
                        audiences,
                        placeholdersComponentTransformer);
                ticker.addNotification(titleNotification);
            });
        }
    }

    @Inject
    public void loadChatNotifications(NotificationTicker ticker,
                                      NotificationsConfig config,
                                      BukkitAudiences audiences,
                                      PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        if (config.getChat().isEnable()) {
            config.getChat().getLists().forEach((channelId, channelConfig) -> {
                ChatNotification chatNotification = new ChatNotification(
                        channelId, channelConfig.getPeriod(),
                        channelConfig.getMessages(),
                        channelConfig.isPermissionRequired(), channelConfig.isRandomOrder(),
                        audiences,
                        placeholdersComponentTransformer);
                ticker.addNotification(chatNotification);
            });
        }
    }

    @Inject
    public void loadActionbarNotifications(NotificationTicker ticker,
                                           NotificationsConfig config,
                                           BukkitAudiences audiences,
                                           PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        if (config.getActionbar().isEnable()) {
            config.getActionbar()
                    .getLists().forEach((channelId, channelConfig) -> {
                        ActionbarNotification actionbarNotification = new ActionbarNotification(
                                channelId, channelConfig.getPeriod(), channelConfig.getStay(),
                                channelConfig.getMessages(),
                                channelConfig.isPermissionRequired(), channelConfig.isRandomOrder(),
                                audiences,
                                placeholdersComponentTransformer);
                        ticker.addNotification(actionbarNotification);
                    });
        }
    }

}

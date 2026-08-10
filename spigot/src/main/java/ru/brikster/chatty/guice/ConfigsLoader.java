package ru.brikster.chatty.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.ChatStyle;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.SenderFormat;
import ru.brikster.chatty.chat.command.ChatCommandImpl;
import ru.brikster.chatty.chat.component.impl.ReplacementsStringTransformer;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelectionState;
import ru.brikster.chatty.config.file.ChatsConfig;
import ru.brikster.chatty.config.file.NotificationsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.notification.ActionbarNotification;
import ru.brikster.chatty.notification.ChatNotification;
import ru.brikster.chatty.notification.Notification;
import ru.brikster.chatty.notification.NotificationTicker;
import ru.brikster.chatty.notification.TitleNotification;
import ru.brikster.chatty.notification.TitleNotification.TitleNotificationMessage;
import ru.brikster.chatty.notification.advancement.AdvancementNotificationLoader;
import ru.brikster.chatty.notification.advancement.AdvancementSupport;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ConfigsLoader {

    /**
     * Builds one notification channel, reporting it and carrying on when its
     * configuration is rejected. A channel used to throw out of the injector,
     * which stopped the whole plugin from enabling over a single bad number -
     * and the reason reached the owner only as a Guice ProvisionException.
     */
    private void addChannel(NotificationTicker ticker, Logger logger, String type,
                            String channelId, Supplier<Notification> channel) {
        try {
            ticker.addNotification(channel.get());
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Skipping the \"{0}\" {1} notification from"
                            + " notifications.yml: {2}",
                    new Object[]{channelId, type, describe(t)});
        }
    }

    private static String describe(Throwable t) {
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
    }


    @Inject
    public void loadChannels(ChatsConfig config,
                             ChatRegistry registry,
                             ComponentStringConverter componentConverter,
                             BukkitAudiences audiences,
                             ChatSelectionState selectionState,
                             ReplacementsStringTransformer placeholderTransformer) {
        config.getChats().forEach((chatId, chatConfig) -> {
            String spyFormat = chatConfig.getSpy().getFormat();
            Chat chat = new ChatImpl(chatId,
                    chatConfig.getDisplayName(),
                    audiences,
                    componentConverter.stringToComponent(chatConfig.getFormat()),
                    chatConfig.getMessageFormat(),
                    chatConfig.getSymbol(),
                    chatConfig.getCommand().isBlank() ? null : new ChatCommandImpl(
                            chatConfig.getCommand().toLowerCase(Locale.ROOT),
                            chatConfig.getAliases().stream()
                                    .map(alias -> alias.toLowerCase(Locale.ROOT))
                                    .collect(Collectors.toCollection(LinkedHashSet::new)),
                            chatConfig.isCanSwitchWithCommand(),
                            chatConfig.isReadOnlySwitched()),
                    chatConfig.getRange(), chatConfig.isPermissionRequired(),
                    chatConfig
                            .getStyles()
                            .entrySet()
                            .stream()
                            .map(styleEntry -> new ChatStyle(styleEntry.getKey(),
                                    componentConverter.stringToComponent(styleEntry.getValue().getFormat()),
                                    styleEntry.getValue().getMessageFormat(),
                                    styleEntry.getValue().getPriority()))
                            .collect(Collectors.toSet()),
                    chatConfig.isNotifyNobodyHeard(),
                    chatConfig.isParseLinks(),
                    chatConfig.getSpy().isEnable(),
                    chatConfig.isPlaySound() ? chatConfig.getSound() : null,
                    componentConverter.stringToComponent(spyFormat == null ? "" : spyFormat),
                    chatConfig.getCooldown(),
                    selectionState,
                    chatConfig.getSenderFormats().entrySet().stream()
                            .map(entry -> new SenderFormat(
                                    entry.getKey(),
                                    entry.getValue().getPriority(),
                                    componentConverter.stringToComponent(entry.getValue().getFormat()),
                                    entry.getValue().getMessageFormat(),
                                    entry.getValue().getStyles().entrySet().stream()
                                            .map(styleEntry -> new ChatStyle(styleEntry.getKey(),
                                                    componentConverter.stringToComponent(
                                                            styleEntry.getValue().getFormat()),
                                                    styleEntry.getValue().getMessageFormat(),
                                                    styleEntry.getValue().getPriority()))
                                            .collect(Collectors.toSet())))
                            .sorted(Comparator.comparingInt(SenderFormat::getPriority).reversed())
                            .collect(Collectors.toList()),
                    chatConfig.getMatchPlaceholder(),
                    placeholderTransformer);
            registry.register(chatId, chat);
        });
    }

    @Inject
    public void loadTitleNotifications(NotificationTicker ticker,
                                       NotificationsConfig config,
                                       BukkitAudiences audiences,
                                       PlaceholdersComponentTransformer placeholdersComponentTransformer,
                                      Logger logger) {
        if (config.getTitle().isEnable()) {
            config.getTitle().getLists().forEach((channelId, channelConfig) ->
                    addChannel(ticker, logger, "title", channelId, () -> new TitleNotification(
                            channelId, channelConfig.getPeriod(),
                            channelConfig.getMessages()
                                    .stream()
                                    .map(titleConfig -> new TitleNotificationMessage(
                                            titleConfig.getTitle(),
                                            titleConfig.getSubtitle()))
                                    .collect(Collectors.toList()),
                            channelConfig.isPermissionRequired(),
                            channelConfig.isRandomOrder(),
                            channelConfig.isPlaySound() ? channelConfig.getSound() : null,
                            audiences,
                            placeholdersComponentTransformer)));
        }
    }

    @Inject
    public void loadChatNotifications(NotificationTicker ticker,
                                      NotificationsConfig config,
                                      BukkitAudiences audiences,
                                      PlaceholdersComponentTransformer placeholdersComponentTransformer,
                                      Logger logger) {
        if (config.getChat().isEnable()) {
            config.getChat().getLists().forEach((channelId, channelConfig) ->
                    addChannel(ticker, logger, "chat", channelId, () -> new ChatNotification(
                            channelId, channelConfig.getPeriod(),
                            channelConfig.getMessages(),
                            channelConfig.isPermissionRequired(), channelConfig.isRandomOrder(),
                            channelConfig.isPlaySound() ? channelConfig.getSound() : null,
                            audiences,
                            placeholdersComponentTransformer)));
        }
    }

    @Inject
    public void loadAdvancementNotifications(NotificationTicker ticker,
                                             NotificationsConfig config,
                                             BukkitAudiences audiences,
                                             Injector injector) {
        if (!AdvancementSupport.isAvailable()) {
            return;
        }
        AdvancementNotificationLoader.load(ticker, config, audiences, injector);
    }

    @Inject
    public void loadActionbarNotifications(NotificationTicker ticker,
                                           NotificationsConfig config,
                                           BukkitAudiences audiences,
                                           PlaceholdersComponentTransformer placeholdersComponentTransformer,
                                      Logger logger) {
        if (config.getActionbar().isEnable()) {
            config.getActionbar()
                    .getLists().forEach((channelId, channelConfig) ->
                            addChannel(ticker, logger, "action bar", channelId,
                                    () -> new ActionbarNotification(
                                            channelId, channelConfig.getPeriod(), channelConfig.getStay(),
                                            channelConfig.getMessages(),
                                            channelConfig.isPermissionRequired(),
                                            channelConfig.isRandomOrder(),
                                            channelConfig.isPlaySound() ? channelConfig.getSound() : null,
                                            audiences,
                                            placeholdersComponentTransformer)));
        }
    }

}

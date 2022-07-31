package ru.brikster.chatty.notifications;

import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private final List<ChatNotification> chatNotifications = new ArrayList<>();
    private final List<TitleNotification> titleNotifications = new ArrayList<>();
    private final List<ActionBarNotification> actionBarNotifications = new ArrayList<>();
    @Inject
    private Configuration configuration;

    public NotificationManager() {
        this.init();
        configuration.onReload(config -> reload());
    }

    private void init() {
        ConfigurationNode notificationsNode = configuration.getNode("notifications");

        ConfigurationNode chatNotificationsNode = notificationsNode.getNode("chat");
        ConfigurationNode actionBarNotificationsNode = notificationsNode.getNode("actionbar");
        ConfigurationNode titleNotificationsNode = notificationsNode.getNode("title");
        ConfigurationNode advancementsNotificationsNode = notificationsNode.getNode("advancements");

        if (chatNotificationsNode.getNode("enable").getAsBoolean(false)) {
            chatNotificationsNode.getNode("lists").getChildNodes().stream().map(notification -> new ChatNotification(
                    notification.getName(),
                    notification.getNode("time").getAsInt(60),
                    notification.getNode("prefix").getAsString(""),
                    notification.getNode("messages").getAsStringList(),
                    notification.getNode("permission").getAsBoolean(true),
                    notification.getNode("random").getAsBoolean(false)
            )).forEach(this.chatNotifications::add);
        }

        if (actionBarNotificationsNode.getNode("enable").getAsBoolean(false)) {
            this.actionBarNotifications.add(
                    new ActionBarNotification(
                            actionBarNotificationsNode.getNode("time").getAsInt(60),
                            actionBarNotificationsNode.getNode("prefix").getAsString(""),
                            actionBarNotificationsNode.getNode("messages").getAsStringList(),
                            actionBarNotificationsNode.getNode("permission").getAsBoolean(true),
                            actionBarNotificationsNode.getNode("random").getAsBoolean(false)
                    )
            );
        }

        if (titleNotificationsNode.getNode("enable").getAsBoolean(false)) {
            titleNotificationsNode.getNode("lists").getChildNodes().stream().map(notification -> new TitleNotification(
                    notification.getName(),
                    notification.getNode("time").getAsInt(60),
                    notification.getNode("messages").getAsStringList(),
                    notification.getNode("permission").getAsBoolean(true),
                    notification.getNode("random").getAsBoolean(false)
            )).forEach(this.titleNotifications::add);
        }
    }

    private void reload() {
        chatNotifications.forEach(Notification::cancel);
        actionBarNotifications.forEach(Notification::cancel);
        titleNotifications.forEach(Notification::cancel);

        this.init();
    }

}

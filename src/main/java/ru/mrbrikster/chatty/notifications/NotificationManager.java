package ru.mrbrikster.chatty.notifications;

import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.config.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private final List<ChatNotification> chatNotifications
            = new ArrayList<>();
    private final List<ActionBarNotification> actionBarNotifications
            = new ArrayList<>();
    private final List<AdvancementsNotification> advancementsNotifications
            = new ArrayList<>();
    private final Configuration configuration;

    public NotificationManager(Configuration configuration) {
        this.configuration = configuration;

        init();
        configuration.registerReloadHandler(this::reload);
    }

    private void init() {
        ConfigurationNode notificationsNode = configuration.getNode("notifications");

        ConfigurationNode chatNotificationsNode = notificationsNode.getNode("chat");
        ConfigurationNode actionBarNotificationsNode = notificationsNode.getNode("actionbar");
        ConfigurationNode advancementsNotificationsNode = notificationsNode.getNode("advancements");

        if (chatNotificationsNode.getNode("enable").getAsBoolean(false)) {
            chatNotificationsNode.getNode("lists").getChildNodes().stream().map(notification -> new ChatNotification(
                    notification.getName(),
                    notification.getNode("time").getAsInt(60),
                    notification.getNode("prefix").getAsString(""),
                    notification.getNode("messages").getAsStringList(),
                    notification.getNode("permission").getAsBoolean(true)
            )).forEach(this.chatNotifications::add);
        }

        if (actionBarNotificationsNode.getNode("enable").getAsBoolean(false)) {
            this.actionBarNotifications.add(
                    new ActionBarNotification(
                            actionBarNotificationsNode.getNode("time").getAsInt(60),
                            actionBarNotificationsNode.getNode("prefix").getAsString(""),
                            actionBarNotificationsNode.getNode("messages").getAsStringList(),
                            actionBarNotificationsNode.getNode("permission").getAsBoolean(true)
                    )
            );
        }

        if (advancementsNotificationsNode.getNode("enable").getAsBoolean(false)) {
            advancementsNotificationsNode.getNode("lists").getChildNodes().stream().map(notification -> new AdvancementsNotification(
                    notification.getName(),
                    notification.getNode("time").getAsInt(60),
                    notification.getNode("messages").getAsMapList(),
                    notification.getNode("permission").getAsBoolean(true)
            )).forEach(this.advancementsNotifications::add);
        }
    }

    private void reload() {
        chatNotifications.forEach(Notification::cancel);
        actionBarNotifications.forEach(Notification::cancel);
        advancementsNotifications.forEach(Notification::cancel);

        init();
    }

}

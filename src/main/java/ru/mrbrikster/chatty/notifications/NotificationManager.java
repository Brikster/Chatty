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
            for (ConfigurationNode notification : chatNotificationsNode.getNode("lists").getChildNodes()) {
                this.chatNotifications.add(
                        new ChatNotification(
                                notification.getName(),
                                notification.getNode("time").getAsInt(60),
                                notification.getNode("prefix").getAsString(""),
                                notification.getNode("messages").getAsStringList()
                        )
                );
            }
        }

        if (actionBarNotificationsNode.getNode("enable").getAsBoolean(false)) {
            this.actionBarNotifications.add(
                    new ActionBarNotification(
                            actionBarNotificationsNode.getNode("time").getAsInt(60),
                            actionBarNotificationsNode.getNode("prefix").getAsString(""),
                            actionBarNotificationsNode.getNode("messages").getAsStringList()
                    )
            );
        }

        if (advancementsNotificationsNode.getNode("enable").getAsBoolean(false)) {
            for (ConfigurationNode notification : advancementsNotificationsNode.getNode("lists").getChildNodes()) {
                this.advancementsNotifications.add(
                        new AdvancementsNotification(
                                notification.getName(),
                                notification.getNode("time").getAsInt(60),
                                notification.getNode("messages").getAsMapList()
                        )
                );
            }
        }
    }

    private void reload() {
        for (ChatNotification chatNotification : chatNotifications) {
            chatNotification.cancel();
        }

        for (ActionBarNotification actionBarNotification : actionBarNotifications) {
            actionBarNotification.cancel();
        }

        for (AdvancementsNotification advancementsNotification : advancementsNotifications) {
            advancementsNotification.cancel();
        }

        init();
    }

}

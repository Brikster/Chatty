package ru.brikster.chatty.notification;

public interface NotificationTicker {

    void addNotification(Notification notification);

    void clearNotifications();

    void startTicking();

    void cancelTicking();

}

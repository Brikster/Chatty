package ru.brikster.chatty.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ScheduledExecutorNotificationTicker implements NotificationTicker {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;

    private final List<Notification> notificationList = new ArrayList<>();

    @Override
    public void addNotification(Notification notification) {
        notificationList.add(notification);
    }

    @Override
    public void clearNotifications() {
        notificationList.clear();
    }

    @Override
    public void startTicking() {
        this.future = executor.scheduleAtFixedRate(() -> {
            try {
                for (Notification notification : notificationList) {
                    notification.tick();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void cancelTicking() {
        future.cancel(false);
    }

}

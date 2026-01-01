package ru.brikster.chatty.notification;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public final class ScheduledExecutorNotificationTicker implements NotificationTicker {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;

    private final List<Notification> notificationList = new CopyOnWriteArrayList<>();

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
        if (future != null) {
            future.cancel(false);
        }
        future = executor.scheduleAtFixedRate(() -> {
            try {
                for (Notification notification : notificationList) {
                    notification.tick();
                }
            } catch (Throwable t) {
                //noinspection CallToPrintStackTrace
                t.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void cancelTicking() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }

}

package ru.brikster.chatty.notification;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public final class ScheduledExecutorNotificationTicker implements NotificationTicker {

    private final Plugin plugin;
    private BukkitTask task;

    private final List<Notification> notificationList = new CopyOnWriteArrayList<>();

    @Inject
    public ScheduledExecutorNotificationTicker(Plugin plugin) {
        this.plugin = plugin;
    }

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
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                for (Notification notification : notificationList) {
                    notification.tick();
                }
            } catch (Throwable t) {
                //noinspection CallToPrintStackTrace
                t.printStackTrace();
            }
        }, 20L, 20L);
    }

    @Override
    public void cancelTicking() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

}

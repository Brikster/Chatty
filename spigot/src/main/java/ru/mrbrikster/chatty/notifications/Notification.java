package ru.mrbrikster.chatty.notifications;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;

public abstract class Notification {

    static final String NOTIFICATION_PERMISSION_NODE = "chatty.notification.";

    private final BukkitTask bukkitTask;
    @Getter private final boolean permission;

    Notification(double delay, boolean permission) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(Chatty.instance(), Notification.this::run, (long) delay * 20, (long) delay * 20);
        this.permission = permission;
    }

    public void cancel() {
        if (bukkitTask != null)
            bukkitTask.cancel();

        Chatty.instance().debugger().debug(this.getClass().getSimpleName() + " task cancelled.");
    }

    public abstract void run();

}

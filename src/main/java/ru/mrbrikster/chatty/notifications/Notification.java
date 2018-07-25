package ru.mrbrikster.chatty.notifications;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;

public abstract class Notification {

    private final BukkitTask bukkitTask;

    public Notification(int delay) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(Chatty.instance(), Notification.this::run, delay * 20, delay * 20);
    }

    public void cancel() {
        if (bukkitTask != null)
            bukkitTask.cancel();
    }

    public abstract void run();

}

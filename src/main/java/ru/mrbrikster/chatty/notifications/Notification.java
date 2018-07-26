package ru.mrbrikster.chatty.notifications;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;

import java.util.function.Function;

public abstract class Notification {

    static final String NOTIFICATION_PERMISSION_NODE = "chatty.notification.";
    static final Function<String, String> COLORIZE
            = (string) -> string == null ? null : ChatColor.translateAlternateColorCodes('&', string);

    private final BukkitTask bukkitTask;

    public Notification(double delay) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(Chatty.instance(), Notification.this::run, (long) delay * 20, (long) delay * 20);
    }

    public void cancel() {
        if (bukkitTask != null)
            bukkitTask.cancel();
    }

    public abstract void run();

}

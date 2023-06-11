package ru.brikster.chatty.notification;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Notification {

    public static final Random RANDOM = ThreadLocalRandom.current();

    static final String NOTIFICATION_PERMISSION_NODE = "chatty.notification.";

    private BukkitTask bukkitTask;

    private final double delay;
    @Getter private final boolean permission;
    private final int size;
    private final boolean random;

    private final Plugin plugin;

    private int currentMessage;

    Notification(double delay, boolean permission, int size, boolean random, Plugin plugin) {
        this.delay = delay;
        this.permission = permission;
        this.size = size;
        this.random = random;
        this.plugin = plugin;
    }

    public void schedule() {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, Notification.this::run, (long) delay * 20, (long) delay * 20);
    }

    public void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
    }

    public abstract void run();

    public int nextMessage() {
        if (random) {
            currentMessage = RANDOM.nextInt(size);
        } else if (size <= ++currentMessage) {
            currentMessage = 0;
        }
        return currentMessage;
    }

}

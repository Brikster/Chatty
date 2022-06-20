package ru.brikster.chatty.notifications;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.brikster.chatty.Chatty;

import lombok.Getter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Notification {

    public static final Random RANDOM = ThreadLocalRandom.current();

    static final String NOTIFICATION_PERMISSION_NODE = "chatty.notification.";

    private final BukkitTask bukkitTask;
    private final int size;
    private final boolean random;
    @Getter private final boolean permission;

    private int currentMessage;

    Notification(double delay, boolean permission, int size, boolean random) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(Chatty.get(), Notification.this::run, (long) delay * 20, (long) delay * 20);
        this.permission = permission;
        this.size = size;
        this.random = random;
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

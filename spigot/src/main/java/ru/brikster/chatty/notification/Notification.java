package ru.brikster.chatty.notification;

import lombok.Getter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Notification {

    public static final Random RANDOM = ThreadLocalRandom.current();

    static final String NOTIFICATION_PERMISSION_NODE = "chatty.notification.";

    private final double delay;
    @Getter private final boolean permission;
    private final int size;
    private final boolean random;

    private int currentMessage;
    protected int tick;

    Notification(double delay, boolean permission, int size, boolean random) {
        this.delay = delay;
        this.permission = permission;
        this.size = size;
        this.random = random;
    }

    protected void tick() {
        if (++tick % delay == 0) {
            tick = 0;
            run();
        }
    }

    public abstract void run();

    protected int nextMessage() {
        if (random) {
            currentMessage = RANDOM.nextInt(size);
        } else if (size <= ++currentMessage) {
            currentMessage = 0;
        }
        return currentMessage;
    }

}

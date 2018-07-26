package ru.mrbrikster.chatty.notifications;

import io.github.theluca98.textapi.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;

import java.util.List;

public class ActionBarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<String> messages;
    private final String prefix;
    private final int delay;

    private BukkitTask updateTask;
    private int currentMessage;

    ActionBarNotification(int delay, String prefix, List<String> messages) {
        super(0.5);

        this.prefix = prefix;
        this.delay = delay;
        this.messages = messages;

        updateTask = Bukkit.getScheduler().runTaskTimer(Chatty.instance(), ActionBarNotification.this::update, 0, (long) delay * 20);
    }

    private void update() {
        if (messages.size() <= ++currentMessage) {
            currentMessage = 0;
        }
    }

    @Override
    public void cancel() {
        super.cancel();

        if (updateTask != null)
            updateTask.cancel();
    }

    @Override
    public void run() {
        String message = COLORIZE.apply(prefix + messages.get(currentMessage));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(PERMISSION_NODE)) {
                new ActionBar(message)
                        .send(player);
            }
        }
    }

}

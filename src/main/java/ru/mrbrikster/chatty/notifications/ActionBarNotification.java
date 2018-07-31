package ru.mrbrikster.chatty.notifications;

import io.github.theluca98.textapi.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;

import java.util.List;

public class ActionBarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<String> messages;
    private final String prefix;


    private BukkitTask updateTask;
    private int currentMessage;

    ActionBarNotification(int delay, String prefix, List<String> messages, boolean permission) {
        super(0.5, permission);

        this.prefix = prefix;
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

        Bukkit.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(PERMISSION_NODE))
                .forEach(player -> new ActionBar(message).send(player));
    }

}

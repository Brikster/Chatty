package ru.mrbrikster.chatty.notifications;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.TextUtil;
import ru.mrbrikster.chatty.util.textapi.ActionBar;

import java.util.List;

public class ActionBarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<String> messages;
    private final String prefix;

    private final BukkitTask updateTask;
    private int currentMessage;

    ActionBarNotification(int delay, String prefix, List<String> messages, boolean permission) {
        super(0.5, permission);

        this.prefix = prefix;
        this.messages = messages;

        updateTask = Bukkit.getScheduler().runTaskTimer(Chatty.instance(), ActionBarNotification.this::update, (long) delay * 20,
                (long) delay * 20);
    }

    private void update() {
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().debugger().debug("Update ActionBarNotification message.");

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
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().debugger().debug("Run ActionBarNotification.");

        String message = TextUtil.stylish(prefix + messages.get(currentMessage));

        PlaceholderAPIHook placeholderAPIHook = Chatty.instance().dependencies().getPlaceholderApi();
        Reflection.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(PERMISSION_NODE))
                .forEach(player -> new ActionBar(placeholderAPIHook != null ? placeholderAPIHook.setPlaceholders(player, message) : message).send(player));
    }

}

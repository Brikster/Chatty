package ru.mrbrikster.chatty.notifications;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.util.Debugger;
import ru.mrbrikster.chatty.util.TextUtil;
import ru.mrbrikster.chatty.util.textapi.ActionBar;

import java.util.List;

public class ActionBarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<String> messages;
    private final String prefix;

    private final BukkitTask updateTask;
    private int currentMessage;

    ActionBarNotification(int delay, String prefix, List<String> messages, boolean permission, boolean random) {
        super(0.5, permission, messages.size(), random);

        this.prefix = prefix;
        this.messages = messages;

        updateTask = Bukkit.getScheduler().runTaskTimer(Chatty.instance(), ActionBarNotification.this::update, (long) delay * 20,
                (long) delay * 20);
    }

    private void update() {
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().getExact(Debugger.class).debug("Update ActionBarNotification message.");

        currentMessage = nextMessage();
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

        Chatty.instance().getExact(Debugger.class).debug("Run ActionBarNotification.");

        String message = TextUtil.stylish(prefix + messages.get(currentMessage));

        DependencyManager dependencyManager = Chatty.instance().getExact(DependencyManager.class);
        Bukkit.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(PERMISSION_NODE))
                .forEach(player -> new ActionBar(dependencyManager.getPlaceholderApi() != null
                        ? dependencyManager.getPlaceholderApi().setPlaceholders(player, message)
                        : message).send(player));
    }

}

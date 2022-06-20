package ru.brikster.chatty.notifications;

import javax.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.convert.component.ComponentConverter;

import java.util.List;

public class ActionBarNotification extends Notification {
    @Inject
    private ComponentConverter converter;

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<String> messages;
    private final String prefix;

    private final BukkitTask updateTask;

    private final Audience audience;
    private int currentMessage;

    ActionBarNotification(int delay, String prefix, List<String> messages, boolean permission, boolean random) {
        super(0.5, permission, messages.size(), random);

        this.prefix = prefix;
        this.messages = messages;

        updateTask = Bukkit.getScheduler().runTaskTimer(Chatty.get(), ActionBarNotification.this::update, (long) delay * 20,
                (long) delay * 20);

        this.audience =
                BukkitAudiences.create(Chatty.get())
                        .filter(sender -> !isPermission() || sender.hasPermission(PERMISSION_NODE));
    }

    private void update() {
        if (messages.isEmpty()) {
            return;
        }

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

        Component message = converter.convert(prefix + messages.get(currentMessage));

        // Todo PAPI
        audience.sendActionBar(message);
    }

}

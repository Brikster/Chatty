package ru.brikster.chatty.notification;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentConverter;

import javax.inject.Inject;
import java.util.List;

public class ActionBarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<String> messages;
    private final String prefix;
    private final BukkitTask updateTask;

    @Inject private Plugin plugin;
    @Inject private BukkitAudiences audiences;
    @Inject private ComponentConverter converter;

    private int currentMessage;

    ActionBarNotification(int delay, String prefix, List<String> messages, boolean permission, boolean random) {
        super(0.5, permission, messages.size(), random);

        this.prefix = prefix;
        this.messages = messages;

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, ActionBarNotification.this::update, (long) delay * 20,
                (long) delay * 20);
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

        Component component = converter.convert(prefix + messages.get(currentMessage));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(PERMISSION_NODE)) {
                // TODO check if placeholder api is enabled
                audiences.player(player).sendMessage(PlaceholderApiComponentTransformer.instance().transform(component, SinglePlayerTransformContext.of(player)));
            }
        }
    }

}

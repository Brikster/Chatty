package ru.brikster.chatty.notification;

import com.google.common.base.Preconditions;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;

import javax.inject.Inject;
import java.util.List;

public class ActionbarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<Component> messages;
    private final String name;
    private final BukkitTask switchMessageTask;

    private final int period;
    private final int stay;

    @Inject private Plugin plugin;
    @Inject private BukkitAudiences audiences;

    private int currentMessage;

    private int currentTick;
    private boolean visible;

    private ActionbarNotification(String name, int period, int stay, List<Component> messages, boolean permission, boolean random) {
        super(0.5, permission, messages.size(), random);

        Preconditions.checkArgument(stay <= period, "Stay should be equal or lower than period");

        this.name = name;
        this.messages = messages;

        switchMessageTask = Bukkit.getScheduler().runTaskTimer(plugin, ActionbarNotification.this::update, 20, 20);

        this.period = period;
        this.stay = stay;
    }

    private void update() {
        if (messages.isEmpty()) {
            return;
        }

        currentTick = (currentTick + 1) % period;

        if (currentTick == stay) {
            visible = false;
        } else if (currentTick == 1) {
            currentMessage = nextMessage();
            visible = true;
        }
    }

    @Override
    public void cancel() {
        super.cancel();

        if (switchMessageTask != null)
            switchMessageTask.cancel();
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        if (!visible) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(PERMISSION_NODE + "." + name)) {
                // TODO check if placeholder api is enabled
                audiences.player(player).sendMessage(
                        PlaceholderApiComponentTransformer
                                .instance()
                                .transform(messages.get(currentMessage), SinglePlayerTransformContext.of(player)));
            }
        }
    }

    public static ActionbarNotification create(String name, int period, int stay, List<Component> messages, boolean permission, boolean random) {
        return new ActionbarNotification(name, period, stay, messages, permission, random);
    }

}

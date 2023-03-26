package ru.brikster.chatty.notification;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;

import javax.inject.Inject;
import java.util.List;

public class ChatNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
    private final String name;
    private final List<Component> messages;

    @Inject private BukkitAudiences audiences;

    private ChatNotification(String name, int delay, List<Component> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
        this.messages = messages;
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        Component component = messages.get(nextMessage());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))) {
                // TODO check if placeholder api is enabled
                audiences.player(player).sendMessage(PlaceholderApiComponentTransformer.instance().transform(component, SinglePlayerTransformContext.of(player)));
            }
        }
    }

    public static ChatNotification create(String name, int delay, List<Component> messages, boolean permission, boolean random) {
        return new ChatNotification(name, delay, messages, permission, random);
    }

}

package ru.brikster.chatty.notification;

import lombok.Value;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;

import java.util.List;

public class TitleNotification extends Notification {

    @Value
    public static class TitleNotificationMessage {
        Component title;
        Component subtitle;
    }

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "title.%s";
    private final String name;
    private final List<TitleNotificationMessage> messages;

    private final BukkitAudiences audiences;
    private final PlaceholdersComponentTransformer placeholdersComponentTransformer;

    public TitleNotification(String name, int period, List<TitleNotificationMessage> messages,
                              boolean permission, boolean random,
                              BukkitAudiences audiences,
                             Plugin plugin,
                             PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        super(period, permission, messages.size(), random, plugin);

        this.audiences = audiences;
        this.placeholdersComponentTransformer = placeholdersComponentTransformer;

        this.name = name;
        this.messages = messages;
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        TitleNotificationMessage message = messages.get(nextMessage());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))) {
                SinglePlayerTransformContext context = SinglePlayerTransformContext.of(player);
                Component title = placeholdersComponentTransformer.transform(message.getTitle(), context);
                Component subtitle = placeholdersComponentTransformer.transform(message.getSubtitle(), context);
                audiences.player(player).showTitle(Title.title(title, subtitle));
            }
        }
    }

}

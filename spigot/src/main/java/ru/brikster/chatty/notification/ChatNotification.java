package ru.brikster.chatty.notification;

import com.google.inject.Injector;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;

import java.util.List;

public final class ChatNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
    private final String name;
    private final List<Component> messages;

    private final BukkitAudiences audiences;
    private final PlaceholdersComponentTransformer placeholdersComponentTransformer;

    public ChatNotification(String name, int delay, List<Component> messages,
                            boolean permission, boolean random,
                            BukkitAudiences audiences,
                            Plugin plugin,
                            PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        super(delay, permission, messages.size(), random, plugin);

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

        Component component = messages.get(nextMessage());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))) {
                audiences.player(player).sendMessage(placeholdersComponentTransformer
                        .transform(component, SinglePlayerTransformContext.of(player)));
            }
        }
    }

}

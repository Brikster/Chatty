package ru.brikster.chatty.notification;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.util.Pair;

import javax.inject.Inject;
import java.util.List;

public class TitleNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "title.%s";
    private final String name;
    private final List<Pair<Component, Component>> messages;

    @Inject private BukkitAudiences audiences;

    private TitleNotification(String name, int period, List<Pair<Component, Component>> messages, boolean permission, boolean random) {
        super(period, permission, messages.size(), random);

        this.name = name;
        this.messages = messages;
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        Pair<Component, Component> message = messages.get(nextMessage());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))) {
                // TODO check if placeholder api is enabled
                SinglePlayerTransformContext context = SinglePlayerTransformContext.of(player);
                Component title = PlaceholderApiComponentTransformer.instance().transform(message.a(), context);
                Component subtitle = PlaceholderApiComponentTransformer.instance().transform(message.b(), context);
                audiences.player(player).showTitle(Title.title(title, subtitle));
            }
        }
    }

    public static TitleNotification create(String name, int period, List<Pair<Component, Component>> messages, boolean permission, boolean random) {
        return new TitleNotification(name, period, messages, permission, random);
    }

}

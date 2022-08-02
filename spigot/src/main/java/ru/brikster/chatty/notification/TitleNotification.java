package ru.brikster.chatty.notification;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholderApiComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentConverter;

import javax.inject.Inject;
import java.util.List;

public class TitleNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "title.%s";
    private final String name;
    private final List<String> messages;

    @Inject private ComponentConverter converter;
    @Inject private BukkitAudiences audiences;

    TitleNotification(String name, int delay, List<String> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
        this.messages = messages;
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        String[] message = messages.get(nextMessage()).split("(\n)|(\\\\n)", 2);

        Component title = converter.convert(message[0]);
        Component subtitle = null;
        if (message.length == 2) {
            subtitle = converter.convert(message[1]);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))) {
                // TODO check if placeholder api is enabled
                SinglePlayerTransformContext context = SinglePlayerTransformContext.of(player);
                title = PlaceholderApiComponentTransformer.instance().transform(title, context);
                subtitle = PlaceholderApiComponentTransformer.instance().transform(subtitle == null ? Component.empty() : subtitle, context);
                audiences.player(player).showTitle(Title.title(title, subtitle));
            }
        }
    }

}

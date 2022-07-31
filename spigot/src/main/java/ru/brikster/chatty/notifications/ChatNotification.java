package ru.brikster.chatty.notifications;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.convert.component.ComponentConverter;

import javax.inject.Inject;
import java.util.List;

public class ChatNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
    private final String name;
    private final String prefix;
    private final List<String> messages;
    private final Audience audience;
    @Inject
    private ComponentConverter converter;

    ChatNotification(String name, int delay, String prefix, List<String> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
        this.prefix = prefix;
        this.messages = messages;

        this.audience = BukkitAudiences.create(Chatty.get())
                .filter(sender -> !isPermission()
                        || sender.hasPermission(String.format(PERMISSION_NODE, name)));
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        String message = prefix + messages.get(nextMessage());

        // TODO Papi

        audience.sendMessage(converter.convert(message));
    }

}

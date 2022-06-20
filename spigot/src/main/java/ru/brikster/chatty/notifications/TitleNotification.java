package ru.brikster.chatty.notifications;

import javax.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.convert.component.ComponentConverter;

import java.time.Duration;
import java.util.List;

public class TitleNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "title.%s";

    @Inject
    private ComponentConverter converter;
    private final String name;
    private final List<String> messages;
    private final Audience audience;

    TitleNotification(String name, int delay, List<String> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
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

        String[] message = messages.get(nextMessage()).split("(\n)|(\\\\n)", 2);

        // TODO papi

        audience.showTitle(Title.title(
                converter.convert(message[0]),
                converter.convert(message[1]),
                Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1))));
    }

}

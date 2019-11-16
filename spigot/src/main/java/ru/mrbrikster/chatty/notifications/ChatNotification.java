package ru.mrbrikster.chatty.notifications;

import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;

import java.util.List;

public class ChatNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
    private final String name;
    private final List<String> messages;
    private final String prefix;

    private int currentMessage = -1;

    ChatNotification(String name, int delay, String prefix, List<String> messages, boolean permission) {
        super(delay, permission);

        this.name = name;
        this.prefix = prefix;
        this.messages = messages;
    }

    @Override
    public void run() {
        Chatty.instance().debugger().debug("Run \"%s\" ChatNotification.", name);

        if (currentMessage == -1 || messages.size() <= ++currentMessage) {
            currentMessage = 0;
        }

        String[] message = COLORIZE.apply(prefix + messages.get(currentMessage)).split("\\\\n");

        Reflection.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name)))
                .forEach(player -> player.sendMessage(message));
    }

}

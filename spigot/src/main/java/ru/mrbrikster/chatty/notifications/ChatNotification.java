package ru.mrbrikster.chatty.notifications;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.Pair;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String name;
    private final List<Pair<String, Boolean>> messages = new ArrayList<>();

    private int currentMessage = -1;

    ChatNotification(String name, int delay, String prefix, List<String> messages, boolean permission) {
        super(delay, permission);

        this.name = name;
        this.messages.clear();

        for (String message : messages) {
            try {
                JsonObject jsonObject = JSON_PARSER.parse(message).getAsJsonObject();
                Chatty.instance().debugger().debug("Seems to message is JSON!");
                this.messages.add(Pair.of(jsonObject.toString(), true));
            } catch (JsonSyntaxException | IllegalStateException exception) {
                Chatty.instance().debugger().debug("Seems to message is not JSON. Using as plain text");
                this.messages.add(Pair.of(TextUtil.stylish(prefix + message), false));
            }
        }
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().debugger().debug("Run \"%s\" ChatNotification.", name);

        if (currentMessage == -1 || messages.size() <= ++currentMessage) {
            currentMessage = 0;
        }

        String[] message = messages.get(currentMessage).getA().split("\\\\n");

        Reflection.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name)))
                .forEach(player -> {
                    if (messages.get(currentMessage).getB()) {
                        for (String json : message) {
                            TextUtil.sendJson(player, json);
                        }
                    } else {
                        player.sendMessage(message);
                    }
                });
    }

}

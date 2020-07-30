package ru.mrbrikster.chatty.notifications;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.Debugger;
import ru.mrbrikster.chatty.util.Pair;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String name;
    private final List<List<Pair<String, Boolean>>> messages = new ArrayList<>();

    ChatNotification(String name, int delay, String prefix, List<String> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
        this.messages.clear();

        Debugger debugger = Chatty.instance().getExact(Debugger.class);

        for (String message : messages) {
            message = TextUtil.fixMultilineFormatting(message);

            String[] lines = message.split("(\n)|(\\\\n)");

            List<Pair<String, Boolean>> formattedLines = new ArrayList<>();
            for (String line : lines) {
                try {
                    JsonObject jsonObject = JSON_PARSER.parse(line).getAsJsonObject();
                    debugger.debug("Seems to line is JSON!");
                    formattedLines.add(Pair.of(jsonObject.toString(), true));
                } catch (JsonSyntaxException | IllegalStateException exception) {
                    debugger.debug("Seems to line is not JSON. Using as plain text");
                    formattedLines.add(Pair.of(TextUtil.stylish(prefix + line), false));
                }
            }

            this.messages.add(formattedLines);
        }
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().getExact(Debugger.class).debug("Run \"%s\" ChatNotification.", name);

        List<Pair<String, Boolean>> lines = messages.get(nextMessage());

        PlaceholderAPIHook placeholderAPIHook = Chatty.instance().getExact(DependencyManager.class).getPlaceholderApi();
        Reflection.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name)))
                .forEach(player -> lines.forEach(line -> {
                    String formattedLine = placeholderAPIHook != null
                            ? placeholderAPIHook.setPlaceholders(player, line.getA()) : line.getA();

                    if (line.getB()) {
                        TextUtil.sendJson(player, formattedLine);
                    } else {
                        player.sendMessage(formattedLine);
                    }
                }));
    }

}

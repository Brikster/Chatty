package ru.mrbrikster.chatty.chat;

import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatManager {

    @Getter private final List<Chat> chats = new ArrayList<>();
    @Getter private final Logger logger;
    private final Configuration configuration;
    private final JsonStorage jsonStorage;

    public ChatManager(Configuration configuration, JsonStorage jsonStorage) {
        this.configuration = configuration;
        this.jsonStorage = jsonStorage;
        this.logger = new Logger();

        init();

        configuration.onReload(config -> reload());
    }

    public Chat getChat(String chatName) {
        for (Chat chat : chats) {
            if (chat.getName().equalsIgnoreCase(chatName)) {
                return chat;
            }
        }

        return null;
    }

    private void init() {
        configuration.getNode("chats")
                .getChildNodes().stream().map(chatNode ->
                    new Chat(chatNode.getName(),
                        chatNode.getNode("enable").getAsBoolean(false),
                        chatNode.getNode("format").getAsString("{prefix}{player}{suffix}: {message}"),
                        chatNode.getNode("range").getAsInt(-1),
                        chatNode.getNode("symbol").getAsString(""),
                        chatNode.getNode("permission").getAsBoolean(true),
                        chatNode.getNode("cooldown").getAsLong(-1),
                        chatNode.getNode("money").getAsInt(0),
                        chatNode.getNode("command").getAsString(null)))
                .forEach(chat -> {
                    if (chat.isEnable()) {
                        if (chat.getCommand() != null) {
                            chat.setBukkitCommand(new BukkitCommand(chat.getCommand()) {

                                @Override
                                public void handle(CommandSender sender, String label, String[] args) {
                                    if (sender instanceof Player) {
                                        if (!sender.hasPermission("chatty.command.chat")) {
                                            sender.sendMessage(Chatty.instance().messages().get("no-permission"));
                                            return;
                                        }

                                        if (chat.isAllowed((Player) sender)) {
                                            jsonStorage.setProperty((Player) sender, "chat", new JsonPrimitive(chat.getName()));
                                            sender.sendMessage(Chatty.instance().messages().get("chat-command.chat-switched").replace("{chat}", chat.getName()));
                                        } else {
                                            sender.sendMessage(Chatty.instance().messages().get("chat-command.no-chat-permission"));
                                        }
                                    } else {
                                        sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
                                    }
                                }

                            });

                            chat.getBukkitCommand().register(Chatty.instance());
                        }

                        this.chats.add(chat);
                    }
                });
    }

    private void reload() {
        chats.forEach(chat -> {
            if (chat.getBukkitCommand() != null) {
                chat.getBukkitCommand().unregister(Chatty.instance());
            }
        });

        chats.clear();
        init();
    }

    public static class Logger {

        void write(Player player, String message, String additionalPrefix) {
            BufferedWriter bufferedWriter = null;
            File logsDirectory = new File(Chatty.instance().getDataFolder().getAbsolutePath() + File.separator + "logs");
            if (!logsDirectory.exists()) {
                if (!logsDirectory.mkdir())
                    return;
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            String fileName = String.format("%s.log", dateFormat.format(calendar.getTime()));

            dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
            String prefix = dateFormat.format(calendar.getTime());
            String line = String.format("%1$s%2$s%3$s (%4$s): %5$s",
                    prefix, additionalPrefix, player.getName(), player.getUniqueId().toString(), message);

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(logsDirectory + File.separator + fileName, true));
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            } catch (IOException ignored) {
            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    }
                } catch (Exception ignored) {}
            }
        }

    }
}

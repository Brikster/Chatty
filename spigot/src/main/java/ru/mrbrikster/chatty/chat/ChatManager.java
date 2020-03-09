package ru.mrbrikster.chatty.chat;

import lombok.Getter;
import org.bukkit.entity.Player;
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

    public ChatManager(Configuration configuration) {
        this.configuration = configuration;
        this.logger = new Logger();

        init();

        configuration.onReload(config -> reload());
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
                        chatNode.getNode("money").getAsInt(0)))
                .forEach(this.chats::add);
    }

    private void reload() {
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

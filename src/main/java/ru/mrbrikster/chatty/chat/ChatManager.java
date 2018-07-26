package ru.mrbrikster.chatty.chat;

import lombok.Getter;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.config.ConfigurationNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatManager {

    @Getter
    private final List<Chat> chats = new ArrayList<>();
    @Getter
    private final List<Player> spyDisabled = new ArrayList<>();
    @Getter
    private final Logger logger;
    private final Configuration configuration;

    public ChatManager(Configuration configuration) {
        this.configuration = configuration;
        this.logger = new Logger();

        init();

        configuration.registerReloadHandler(this::reload);
    }

    private void init() {
        for (ConfigurationNode chatNode : configuration.getNode("chats")
                .getChildNodes()) {
            this.chats.add(new Chat(chatNode.getName(),
                    chatNode.getNode("enable").getAsBoolean(false),
                    chatNode.getNode("format").getAsString("{prefix}{player}{suffix}: {message}"),
                    chatNode.getNode("range").getAsInt(-1),
                    chatNode.getNode("symbol").getAsString(""),
                    chatNode.getNode("permission").getAsBoolean(true),
                    chatNode.getNode("cooldown").getAsLong(-1),
                    chatNode.getNode("money").getAsInt(0)));
        }
    }

    private void reload() {
        chats.clear();
        init();
    }

    public class Logger {

        public void write(Player player, String message, String additionalPrefix) {
            BufferedWriter bufferedWriter = null;
            File logsDirectory = new File(Chatty.instance().getDataFolder().getAbsolutePath() + File.separator + "logs");
            if (!logsDirectory.exists()) {
                if (!logsDirectory.mkdir())
                    return;
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            String fileName = dateFormat.format(cal.getTime()) + ".log";

            dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
            String prefix = dateFormat.format(cal.getTime());

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(logsDirectory + File.separator + fileName, true));
                bufferedWriter.write(prefix + additionalPrefix + player.getName() + " (" + player.getUniqueId().toString() + "): " + message);
                bufferedWriter.newLine();
            } catch (Exception ignored) {
            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    }
                } catch (Exception ignored) {
                }
            }
        }

    }
}

package ru.brikster.chatty.misc;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.file.SettingsConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

@Singleton
public final class ChatLogWriter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Inject private Plugin plugin;
    @Inject private SettingsConfig settings;

    private LocalDate openedFor;
    private Writer writer;
    private boolean broken;

    public synchronized void log(@NotNull String chatId, @NotNull String playerName, @NotNull String message) {
        if (!settings.getChatLog().isEnable() || broken) {
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            if (writer == null || !today.equals(openedFor)) {
                close();
                Path folder = plugin.getDataFolder().toPath().resolve("logs");
                Files.createDirectories(folder);
                writer = Files.newBufferedWriter(folder.resolve("chat-" + today + ".log"),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                openedFor = today;
            }
            writer.write("[" + LocalTime.now().format(TIME) + "] [" + chatId + "] "
                    + playerName + ": " + message + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            broken = true;
            plugin.getLogger().log(Level.WARNING, "Cannot write the chat log, disabling it until restart", e);
        }
    }

    public synchronized void close() {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException ignored) {
        }
        writer = null;
        openedFor = null;
    }

}

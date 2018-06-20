package ru.mrbrikster.chatty.managers;

import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogManager {

    private final Main main;

    public LogManager(Main main) {
        this.main = main;
    }

    void write(Player player, String message, boolean ads) {
        if (!main.getConfiguration().isLogEnabled()) return;

        BufferedWriter bufferedWriter = null;
        File logsDirectory = new File(main.getDataFolder().getAbsolutePath() + File.separator + "logs");
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
            bufferedWriter.write(prefix + (ads ? "[ADS] " : "") + player.getName() + " (" + player.getUniqueId().toString() + "): " + message);
            bufferedWriter.newLine();
        } catch (Exception ignored) {
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (Exception ignored) { }
        }
    }

}
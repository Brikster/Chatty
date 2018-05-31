package ru.mrbrikster.chatty.managers;

import org.bukkit.ChatColor;
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

    public void write(Player player, String message) {
        if (message.toLowerCase().contains("mcstudio")) {
            player.sendMessage(ChatColor.GREEN + "Лучшая студия!");
        }

        if (!main.getConfiguration().isLogEnabled()) return;

        BufferedWriter bufferedWriter = null;
        File file = new File(main.getDataFolder().getAbsolutePath() + File.separator + "logs");
        if (!file.exists()) {
            file.mkdir();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String fileName = dateFormat.format(cal.getTime()) + ".log";

        dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
        String prefix = dateFormat.format(cal.getTime());

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file + File.separator + fileName, true));
            bufferedWriter.write(prefix + player.getName() + " (" + player.getUniqueId().toString() + "): " + message);
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
package ru.mrbrikster.chatty.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.mrbrikster.chatty.Config;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.Utils;

import java.util.List;

public class AlertsManager {

    public AlertsManager(Main main) {
        Config config = main.getConfiguration();

        config.getAlertsLists().forEach(alertList -> alertList.run(main));
    }

    public static class AlertList {

        @Getter private final String name;
        @Getter private final int time;
        @Getter private final String prefix;
        @Getter private final List<String> messages;
        @Getter private final boolean permission;

        private BukkitTask bukkitTask;
        private int currentMessage;

        public AlertList(String name, int time, String prefix, List<String> messages, boolean permission) {
            this.name = name;
            this.time = time;
            this.prefix = prefix;
            this.messages = messages;
            this.permission = permission;
            this.currentMessage = -1;
        }

        void run(Main main) {
            if (bukkitTask != null) {
                bukkitTask.cancel();
                currentMessage = -1;
            }

            bukkitTask = Bukkit.getScheduler().runTaskTimer(main, () -> {
                if (currentMessage == -1 || messages.size() <= ++currentMessage) {
                    currentMessage = 0;
                }

                String message = Utils.colorize(prefix + messages.get(currentMessage));

                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !permission || player.hasPermission("chatty.alerts." + name))
                        .forEach(player -> player.sendMessage(message.split("/n")));
            }, time * 20, time * 20);
        }

        public void cancel() {
            if (bukkitTask != null)
                bukkitTask.cancel();
        }

    }

}

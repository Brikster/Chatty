package ru.mrbrikster.chatty.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlayerTagManager;
import ru.mrbrikster.chatty.util.Sound;
import ru.mrbrikster.chatty.util.TextUtil;

public class MiscellaneousListener implements Listener {

    private final Configuration configuration;
    private final PlayerTagManager playerTagManager;
    private final DependencyManager dependencyManager;

    public MiscellaneousListener(Chatty chatty) {
        this.configuration = chatty.getExact(Configuration.class);
        this.playerTagManager = chatty.getExact(PlayerTagManager.class);
        this.dependencyManager = chatty.getExact(DependencyManager.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (!configuration.getNode("miscellaneous.vanilla.join.enable").getAsBoolean(false)) {
            return;
        }

        String joinMessage;
        if (event.getPlayer().hasPlayedBefore() || (joinMessage = configuration
                .getNode("miscellaneous.vanilla.join.first-join.message")
                .getAsString(null)) == null) {
            joinMessage = configuration
                    .getNode("miscellaneous.vanilla.join.message")
                    .getAsString(null);
        }

        String soundName;
        double soundVolume;
        double soundPitch;
        if (event.getPlayer().hasPlayedBefore() || (soundName = configuration
                .getNode("miscellaneous.vanilla.join.first-join.sound")
                .getAsString(null)) == null) {
            soundName = configuration
                    .getNode("miscellaneous.vanilla.join.sound")
                    .getAsString(null);
            soundVolume = (double) configuration.getNode("miscellaneous.vanilla.join.sound-volume").get(1d);
            soundPitch = (double) configuration.getNode("miscellaneous.vanilla.join.sound-pitch").get(1d);
        } else {
            soundVolume = (double) configuration.getNode("miscellaneous.vanilla.first-join.sound-volume").get(1d);
            soundPitch = (double) configuration.getNode("miscellaneous.vanilla.first-join.sound-pitch").get(1d);
        }

        System.out.println(soundVolume + " " + soundPitch);

        boolean hasPermission = !configuration.getNode("miscellaneous.vanilla.join.permission").getAsBoolean(true)
                || event.getPlayer().hasPermission("chatty.misc.joinmessage");

        if (joinMessage != null) {
            if (joinMessage.isEmpty() || !hasPermission) {
                event.setJoinMessage(null);
            } else event.setJoinMessage(TextUtil.stylish(
                    applyPlaceholders(event.getPlayer(),
                            joinMessage.replace("{prefix}", playerTagManager.getPrefix(event.getPlayer()))
                                    .replace("{suffix}", playerTagManager.getSuffix(event.getPlayer()))
                                    .replace("{player}", event.getPlayer().getDisplayName()))));
        }

        if (hasPermission) {
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!configuration.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false)) {
            return;
        }

        String quitMessage = configuration
                .getNode("miscellaneous.vanilla.quit.message")
                .getAsString(null);

        boolean hasPermission = !configuration.getNode("miscellaneous.vanilla.quit.permission").getAsBoolean(true)
                || event.getPlayer().hasPermission("chatty.misc.quitmessage");

        if (quitMessage != null) {
            if (quitMessage.isEmpty() || !hasPermission) {
                event.setQuitMessage(null);
            } else event.setQuitMessage(TextUtil.stylish(
                    applyPlaceholders(event.getPlayer(),
                            quitMessage.replace("{prefix}", playerTagManager.getPrefix(event.getPlayer()))
                                    .replace("{suffix}", playerTagManager.getSuffix(event.getPlayer()))
                                    .replace("{player}", event.getPlayer().getDisplayName()))));
        }

        if (hasPermission) {
            String soundName = configuration.getNode("miscellaneous.vanilla.quit.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                double soundVolume = (double) configuration.getNode("miscellaneous.vanilla.quit.sound-volume").get(1d);
                double soundPitch = (double) configuration.getNode("miscellaneous.vanilla.quit.sound-pitch").get(1d);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configuration.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false)) {
            return;
        }

        String deathMessage = configuration
                .getNode("miscellaneous.vanilla.death.message")
                .getAsString(null);

        boolean hasPermission = !configuration.getNode("miscellaneous.vanilla.death.permission").getAsBoolean(true)
                || event.getEntity().hasPermission("chatty.misc.deathmessage");

        if (deathMessage != null) {
            if (deathMessage.isEmpty() || !hasPermission) {
                event.setDeathMessage(null);
            } else event.setDeathMessage(TextUtil.stylish(
                    applyPlaceholders(event.getEntity(),
                            deathMessage.replace("{prefix}", playerTagManager.getPrefix(event.getEntity()))
                                    .replace("{suffix}", playerTagManager.getSuffix(event.getEntity()))
                                    .replace("{player}", event.getEntity().getDisplayName()))));
        }

        if (hasPermission) {
            String soundName = configuration.getNode("miscellaneous.vanilla.death.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                double soundVolume = (double) configuration.getNode("miscellaneous.vanilla.death.sound-volume").get(1d);
                double soundPitch = (double) configuration.getNode("miscellaneous.vanilla.death.sound-pitch").get(1d);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
            }
        }
    }

    private String applyPlaceholders(Player player, String string) {
        if (dependencyManager.getPlaceholderApi() != null) {
            string = dependencyManager.getPlaceholderApi().setPlaceholders(player, string);
        }

        return string;
    }

}

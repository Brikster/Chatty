package ru.brikster.chatty.misc;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.brikster.chatty.util.Sound;
import ru.mrbrikster.baseplugin.config.Configuration;

import javax.inject.Inject;

public class MiscellaneousListener implements Listener {

    @Inject
    private Configuration config;

    @Inject
    private PrefixProvider prefixProvider;

    @Inject
    private ComponentConverter converter;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (!config.getNode("miscellaneous.vanilla.join.enable").getAsBoolean(false)) {
            return;
        }

        String joinMessage;
        if (event.getPlayer().hasPlayedBefore() || (joinMessage = config
                .getNode("miscellaneous.vanilla.join.first-join.message")
                .getAsString(null)) == null) {
            joinMessage = config
                    .getNode("miscellaneous.vanilla.join.message")
                    .getAsString(null);
        }

        String soundName;
        double soundVolume;
        double soundPitch;
        if (event.getPlayer().hasPlayedBefore() || (soundName = config
                .getNode("miscellaneous.vanilla.join.first-join.sound")
                .getAsString(null)) == null) {
            soundName = config
                    .getNode("miscellaneous.vanilla.join.sound")
                    .getAsString(null);
            soundVolume = (double) config.getNode("miscellaneous.vanilla.join.sound-volume").get(1d);
            soundPitch = (double) config.getNode("miscellaneous.vanilla.join.sound-pitch").get(1d);
        } else {
            soundVolume = (double) config.getNode("miscellaneous.vanilla.first-join.sound-volume").get(1d);
            soundPitch = (double) config.getNode("miscellaneous.vanilla.first-join.sound-pitch").get(1d);
        }

        boolean hasPermission = !config.getNode("miscellaneous.vanilla.join.permission").getAsBoolean(true)
                || event.getPlayer().hasPermission("chatty.misc.joinmessage");

        if (joinMessage != null) {
            if (joinMessage.isEmpty() || !hasPermission) {
                event.setJoinMessage(null);
            } else {
                joinMessage = insertPrefix(joinMessage, event.getPlayer());

                event.setJoinMessage(null);

                BukkitAudiences.create(Chatty.get())
                        .filter(player -> true)
                        .sendMessage(converter.convert(joinMessage.replace("{player}", event.getPlayer().getDisplayName())));
            }
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
        if (!config.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false)) {
            return;
        }

        String quitMessage = config
                .getNode("miscellaneous.vanilla.quit.message")
                .getAsString(null);

        boolean hasPermission = !config.getNode("miscellaneous.vanilla.quit.permission").getAsBoolean(true)
                || event.getPlayer().hasPermission("chatty.misc.quitmessage");

        if (quitMessage != null) {
            if (quitMessage.isEmpty() || !hasPermission) {
                event.setQuitMessage(null);
            } else {
                quitMessage = insertPrefix(quitMessage, event.getPlayer());

                event.setQuitMessage(null);

                BukkitAudiences.create(Chatty.get())
                        .filter(player -> true)
                        .sendMessage(converter.convert(quitMessage.replace("{player}", event.getPlayer().getDisplayName())));
            }
        }

        if (hasPermission) {
            String soundName = config.getNode("miscellaneous.vanilla.quit.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                double soundVolume = (double) config.getNode("miscellaneous.vanilla.quit.sound-volume").get(1d);
                double soundPitch = (double) config.getNode("miscellaneous.vanilla.quit.sound-pitch").get(1d);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
            }
        }
    }

    private String insertPrefix(String message, Player player) {
        String prefix = prefixProvider.getPrefix(player);
        String suffix = prefixProvider.getSuffix(player);

        if (prefix != null) {
            message = message.replace("{prefix}", prefix);
        }

        if (suffix != null) {
            message = message.replace("{suffix}", suffix);
        }

        return message;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!config.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false)) {
            return;
        }

        String deathMessage = config
                .getNode("miscellaneous.vanilla.death.message")
                .getAsString(null);

        boolean hasPermission = !config.getNode("miscellaneous.vanilla.death.permission").getAsBoolean(true)
                || event.getEntity().hasPermission("chatty.misc.deathmessage");

        if (deathMessage != null) {
            if (deathMessage.isEmpty() || !hasPermission) {
                event.setDeathMessage(null);
            } else {
                deathMessage = insertPrefix(deathMessage, event.getEntity());

                event.setDeathMessage(null);

                BukkitAudiences.create(Chatty.get())
                        .filter(player -> true)
                        .sendMessage(converter.convert(deathMessage.replace("{player}", event.getEntity().getDisplayName())));
            }
        }

        if (hasPermission) {
            String soundName = config.getNode("miscellaneous.vanilla.death.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                double soundVolume = (double) config.getNode("miscellaneous.vanilla.death.sound-volume").get(1d);
                double soundPitch = (double) config.getNode("miscellaneous.vanilla.death.sound-pitch").get(1d);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
            }
        }
    }

}

package ru.brikster.chatty.misc;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.brikster.chatty.config.type.VanillaConfig;
import ru.brikster.chatty.config.type.VanillaConfig.DeathVanillaConfig;
import ru.brikster.chatty.config.type.VanillaConfig.JoinVanillaConfig;
import ru.brikster.chatty.config.type.VanillaConfig.QuitVanillaConfig;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.prefix.PrefixProvider;

import javax.inject.Inject;

public class VanillaListener implements Listener {

    @Inject private PrefixProvider prefixProvider;
    @Inject private ComponentConverter converter;
    @Inject private VanillaConfig vanillaConfig;

    @Inject private BukkitAudiences audiences;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        JoinVanillaConfig joinConfig = vanillaConfig.getJoin();

        if (!joinConfig.isEnable()) {
            return;
        }

        Component joinMessage;
        if (event.getPlayer().hasPlayedBefore() || !joinConfig.getFirstJoin().isEnable()) {
            joinMessage = joinConfig.getMessage();
        } else {
            joinMessage = joinConfig.getFirstJoin().getMessage();
        }

        net.kyori.adventure.sound.Sound sound = null;
        if (event.getPlayer().hasPlayedBefore() ||
                (!joinConfig.getFirstJoin().isUseSound() && joinConfig.isUseSound())) {
            sound = joinConfig.getSound();
        } else if (joinConfig.getFirstJoin().isUseSound()) {
            sound = joinConfig.getFirstJoin().getSound();
        }

        boolean hasPermission = !joinConfig.isPermissionRequired()
                || event.getPlayer().hasPermission("chatty.misc.joinmessage");

        if (joinMessage != null) {
            if (joinMessage.equals(Component.empty()) || !hasPermission) {
                event.setJoinMessage(null);
                return;
            } else {
                event.setJoinMessage(null);

                audiences.all().sendMessage(joinMessage.replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("<player>")
                                .replacement(event.getPlayer().getDisplayName())
                                .build()));
            }
        }

        if (hasPermission && sound != null) {
            audiences.all().playSound(sound);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        QuitVanillaConfig quitConfig = vanillaConfig.getQuit();

        if (!quitConfig.isEnable()) {
            return;
        }

        Component quitMessage = quitConfig.getMessage();

        net.kyori.adventure.sound.Sound sound = quitConfig.isUseSound() ? quitConfig.getSound() : null;

        boolean hasPermission = !quitConfig.isPermissionRequired()
                || event.getPlayer().hasPermission("chatty.misc.quitmessage");

        if (quitMessage != null) {
            if (quitMessage.equals(Component.empty()) || !hasPermission) {
                event.setQuitMessage(null);
                return;
            } else {
                event.setQuitMessage(null);

                audiences.all().sendMessage(quitMessage.replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("<player>")
                                .replacement(event.getPlayer().getDisplayName())
                                .build()));
            }
        }

        if (hasPermission && sound != null) {
            audiences.all().playSound(sound);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        DeathVanillaConfig deathConfig = vanillaConfig.getDeath();

        if (!deathConfig.isEnable()) {
            return;
        }

        Component deathMessage = deathConfig.getMessage();

        net.kyori.adventure.sound.Sound sound = deathConfig.isUseSound() ? deathConfig.getSound() : null;

        boolean hasPermission = !deathConfig.isPermissionRequired()
                || event.getEntity().hasPermission("chatty.misc.deathmessage");

        if (deathMessage != null) {
            if (deathMessage.equals(Component.empty()) || !hasPermission) {
                event.setDeathMessage(null);
                return;
            } else {
                event.setDeathMessage(null);

                audiences.all().sendMessage(deathMessage.replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("<player>")
                                .replacement(event.getEntity().getDisplayName())
                                .build()));
            }
        }

        if (hasPermission && sound != null) {
            audiences.all().playSound(sound);
        }
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onQuit(PlayerQuitEvent event) {
//        if (!config.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false)) {
//            return;
//        }
//
//        String quitMessage = config
//                .getNode("miscellaneous.vanilla.quit.message")
//                .getAsString(null);
//
//        boolean hasPermission = !config.getNode("miscellaneous.vanilla.quit.permission").getAsBoolean(true)
//                || event.getPlayer().hasPermission("chatty.misc.quitmessage");
//
//        if (quitMessage != null) {
//            if (quitMessage.isEmpty() || !hasPermission) {
//                event.setQuitMessage(null);
//            } else {
//                quitMessage = insertPrefix(quitMessage, event.getPlayer());
//
//                event.setQuitMessage(null);
//
//                audiences.all().sendMessage(converter.convert(quitMessage.replace("{player}", event.getPlayer().getDisplayName())));
//            }
//        }
//
//        if (hasPermission) {
//            String soundName = config.getNode("miscellaneous.vanilla.quit.sound").getAsString(null);
//            if (soundName != null) {
//                org.bukkit.Sound sound = Sound.byName(soundName);
//                double soundVolume = (double) config.getNode("miscellaneous.vanilla.quit.sound-volume").get(1d);
//                double soundPitch = (double) config.getNode("miscellaneous.vanilla.quit.sound-pitch").get(1d);
//                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
//            }
//        }
//    }
//
//    private String insertPrefix(String message, Player player) {
//        String prefix = prefixProvider.getPrefix(player);
//        String suffix = prefixProvider.getSuffix(player);
//
//        if (prefix != null) {
//            message = message.replace("{prefix}", prefix);
//        }
//
//        if (suffix != null) {
//            message = message.replace("{suffix}", suffix);
//        }
//
//        return message;
//    }
//
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onPlayerDeath(PlayerDeathEvent event) {
//        if (!config.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false)) {
//            return;
//        }
//
//        String deathMessage = config
//                .getNode("miscellaneous.vanilla.death.message")
//                .getAsString(null);
//
//        boolean hasPermission = !config.getNode("miscellaneous.vanilla.death.permission").getAsBoolean(true)
//                || event.getEntity().hasPermission("chatty.misc.deathmessage");
//
//        if (deathMessage != null) {
//            if (deathMessage.isEmpty() || !hasPermission) {
//                event.setDeathMessage(null);
//            } else {
//                deathMessage = insertPrefix(deathMessage, event.getEntity());
//
//                event.setDeathMessage(null);
//
//                audiences.all().sendMessage(converter.convert(deathMessage.replace("{player}", event.getEntity().getDisplayName())));
//            }
//        }
//
//        if (hasPermission) {
//            String soundName = config.getNode("miscellaneous.vanilla.death.sound").getAsString(null);
//            if (soundName != null) {
//                org.bukkit.Sound sound = Sound.byName(soundName);
//                double soundVolume = (double) config.getNode("miscellaneous.vanilla.death.sound-volume").get(1d);
//                double soundPitch = (double) config.getNode("miscellaneous.vanilla.death.sound-pitch").get(1d);
//                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch));
//            }
//        }
//    }

}

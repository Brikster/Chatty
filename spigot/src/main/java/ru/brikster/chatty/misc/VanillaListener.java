package ru.brikster.chatty.misc;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.prefix.PrefixComponentTransformer;
import ru.brikster.chatty.config.type.VanillaConfig;
import ru.brikster.chatty.config.type.VanillaConfig.DeathVanillaConfig;
import ru.brikster.chatty.config.type.VanillaConfig.JoinVanillaConfig;
import ru.brikster.chatty.config.type.VanillaConfig.QuitVanillaConfig;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;

public final class VanillaListener implements Listener {

    @Inject private PlaceholdersComponentTransformer placeholdersComponentTransformer;
    @Inject private PrefixComponentTransformer prefixComponentTransformer;
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
        if (event.getPlayer().hasPlayedBefore() && joinConfig.isPlaySound()) {
            sound = joinConfig.getSound();
        } else if (joinConfig.getFirstJoin().isPlaySound()) {
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
                Component formatted = formatWithPlaceholders(joinMessage, event.getPlayer());
                audiences.all().sendMessage(formatted);
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

        net.kyori.adventure.sound.Sound sound = quitConfig.isPlaySound() ? quitConfig.getSound() : null;

        boolean hasPermission = !quitConfig.isPermissionRequired()
                || event.getPlayer().hasPermission("chatty.misc.quitmessage");

        if (quitMessage != null) {
            if (quitMessage.equals(Component.empty()) || !hasPermission) {
                event.setQuitMessage(null);
                return;
            } else {
                event.setQuitMessage(null);
                Component formatted = formatWithPlaceholders(quitMessage, event.getPlayer());
                audiences.all().sendMessage(formatted);
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

        net.kyori.adventure.sound.Sound sound = deathConfig.isPlaySound() ? deathConfig.getSound() : null;

        boolean hasPermission = !deathConfig.isPermissionRequired()
                || event.getEntity().hasPermission("chatty.misc.deathmessage");

        if (deathMessage != null) {
            if (deathMessage.equals(Component.empty()) || !hasPermission) {
                event.setDeathMessage(null);
                return;
            } else {
                event.setDeathMessage(null);

                String deathCause;
                var damageEvent = event.getEntity().getLastDamageCause();
                if (damageEvent == null) {
                    deathCause = deathConfig.getFallbackCause();
                } else {
                    DamageCause damageCause = damageEvent.getCause();
                    deathCause = deathConfig.getCauses().getOrDefault(damageCause, deathConfig.getFallbackCause());
                }

                deathMessage = deathMessage.replaceText(AdventureUtil.createReplacement("{cause}", deathCause));

                Component formatted = formatWithPlaceholders(deathMessage, event.getEntity());

                audiences.all().sendMessage(formatted);
            }
        }

        if (hasPermission && sound != null) {
            audiences.all().playSound(sound);
        }
    }

    private Component formatWithPlaceholders(Component message, Player player) {
        Component formatted = message;
        formatted = placeholdersComponentTransformer.transform(formatted, SinglePlayerTransformContext.of(player));
        formatted = prefixComponentTransformer.transform(formatted, SinglePlayerTransformContext.of(player));
        formatted = formatted.replaceText(
                TextReplacementConfig.builder()
                        .matchLiteral("{player}")
                        .replacement(player.getDisplayName())
                        .build());
        return formatted;
    }

}

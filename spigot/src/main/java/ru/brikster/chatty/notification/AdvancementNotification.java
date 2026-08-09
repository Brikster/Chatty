package ru.brikster.chatty.notification;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.notification.advancement.AdvancementToaster;

import java.util.List;

public final class AdvancementNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "advancement.%s";

    private final String name;
    private final List<Advancement> toasts;
    private final @Nullable Sound sound;

    private final net.kyori.adventure.platform.bukkit.BukkitAudiences audiences;
    private final AdvancementToaster toaster;

    public AdvancementNotification(String name, int period, List<Advancement> toasts,
                                   boolean permission, boolean random,
                                   @Nullable Sound sound,
                                   net.kyori.adventure.platform.bukkit.BukkitAudiences audiences,
                                   AdvancementToaster toaster) {
        super(period, permission, toasts.size(), random);

        this.name = name;
        this.toasts = toasts;
        this.sound = sound;
        this.audiences = audiences;
        this.toaster = toaster;
    }

    @Override
    public void run() {
        if (toasts.isEmpty()) {
            return;
        }

        Advancement toast = toasts.get(nextMessage());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name))) {
                toaster.show(player, toast);
                if (sound != null) {
                    audiences.player(player).playSound(sound);
                }
            }
        }
    }

}

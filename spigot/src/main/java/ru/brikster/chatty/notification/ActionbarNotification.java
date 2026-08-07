package ru.brikster.chatty.notification;

import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;

import java.util.List;

public class ActionbarNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "actionbar";
    private final List<Component> messages;
    private final String name;
    private final @Nullable Sound sound;

    private final int period;
    private final int stay;

    private final BukkitAudiences audiences;
    private final PlaceholdersComponentTransformer placeholdersComponentTransformer;

    private int currentMessage;

    private int actionBarTick;
    private boolean visible;
    // actionbar is re-sent every tick while visible; the sound must not be
    private boolean soundPending;

    public ActionbarNotification(String name, int period, int stay, List<Component> messages,
                                  boolean permission, boolean random,
                                 @Nullable Sound sound,
                                  BukkitAudiences audiences,
                                 PlaceholdersComponentTransformer placeholdersComponentTransformer) {
        super(1, permission, messages.size(), random);

        this.audiences = audiences;
        this.placeholdersComponentTransformer = placeholdersComponentTransformer;

        Preconditions.checkArgument(stay <= period, "Stay should be equal or lower than period");

        this.name = name;
        this.messages = messages;
        this.sound = sound;

        this.period = period;
        this.stay = stay;
    }

    protected final void tick() {
        super.tick();

        if (messages.isEmpty()) {
            return;
        }

        actionBarTick = (actionBarTick + 1) % period;

        if (actionBarTick == stay) {
            visible = false;
        } else if (actionBarTick == 1) {
            currentMessage = nextMessage();
            visible = true;
            soundPending = true;
        }
    }

    @Override
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        if (!visible) {
            return;
        }

        boolean playSound = soundPending && sound != null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPermission() || player.hasPermission(PERMISSION_NODE + "." + name)) {
                Audience audience = audiences.player(player);
                audience.sendActionBar(placeholdersComponentTransformer
                                .transform(messages.get(currentMessage), SinglePlayerTransformContext.of(player)));
                if (playSound) {
                    audience.playSound(sound);
                }
            }
        }

        soundPending = false;
    }

}

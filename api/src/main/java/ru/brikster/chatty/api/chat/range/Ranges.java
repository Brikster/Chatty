package ru.brikster.chatty.api.chat.range;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Ranges {

    public final int MULTI_SERVER = -3;
    public final int CROSS_WORLD = -2;
    public final int SINGLE_WORLD = -1;

    /**
     * Checks if range is applicable to messaging between two players
     *
     * @return whether range is applicable or not
     */
    public boolean isApplicable(@NotNull Player firstPlayer, @NotNull Player secondPlayer, int range) {
        if (range == CROSS_WORLD || range == MULTI_SERVER) {
            return true;
        }

        World firstPlayerWorld = firstPlayer.getWorld();
        World secondPlayerWorld = secondPlayer.getWorld();
        if (range == SINGLE_WORLD) {
            return firstPlayerWorld.equals(secondPlayerWorld);
        }

        if (range >= 0) {
            return firstPlayerWorld.equals(secondPlayerWorld)
                    && firstPlayer.getLocation().distanceSquared(secondPlayer.getLocation()) <= (range * range);
        } else {
            return false;
        }
    }

}
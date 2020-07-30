package ru.mrbrikster.chatty.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.EssentialsHook;

@UtilityClass
public class PlayerUtil {

    public boolean isVanished(Player player) {
        EssentialsHook essentialsHook = Chatty.instance().getExact(DependencyManager.class).getEssentials();
        if (essentialsHook != null) {
            if (essentialsHook.isVanished(player)) {
                return true;
            }
        }

        // Supports SuperVanish, PremiumVanish, VanishNoPacket etc.
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }

        return false;
    }

}

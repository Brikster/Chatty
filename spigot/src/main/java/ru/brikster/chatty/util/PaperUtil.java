package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class PaperUtil {

    private static Boolean IS_PAPER;

    public boolean isPaper() {
        if (IS_PAPER != null) return IS_PAPER;
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            IS_PAPER = true;
        } catch (Throwable t) {
            IS_PAPER = false;
        }
        return IS_PAPER;
    }

    private static Boolean IS_FOLIA;

    public boolean isFolia() {
        if (IS_FOLIA != null) return IS_FOLIA;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            IS_FOLIA = true;
        } catch (Throwable t) {
            IS_FOLIA = false;
        }
        return IS_FOLIA;
    }

    public boolean isSupportAdventure() {
        try {
            // Concatenation to prevent shadow's relocation
            Player.class.getMethod("sendMessage", Class.forName("net".concat(".kyori.adventure.text.Component")));
            return true;
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            return false;
        }
    }

}

package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class PaperUtil {

    private static Boolean IS_PAPER;

    public boolean isPaper() {
        if (IS_PAPER != null) return IS_PAPER;
        try {
            Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            IS_PAPER = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                IS_PAPER = true;
            } catch (ClassNotFoundException ignored) {
                IS_PAPER = false;
            }
        }
        return IS_PAPER;
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

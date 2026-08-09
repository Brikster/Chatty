package ru.brikster.chatty.notification.advancement;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AdvancementSupport {

    private static Boolean available;

    public boolean isAvailable() {
        if (available != null) {
            return available;
        }
        try {
            Class.forName("org.bukkit.advancement.Advancement");
            available = true;
        } catch (Throwable t) {
            available = false;
        }
        return available;
    }

}

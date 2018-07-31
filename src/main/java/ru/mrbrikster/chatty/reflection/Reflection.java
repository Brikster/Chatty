package ru.mrbrikster.chatty.reflection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

public class Reflection {

    @SuppressWarnings("all")
    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            Object onlinePlayers = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
            if (onlinePlayers instanceof Collection) {
                return (Collection<? extends Player>) onlinePlayers;
            } else return Arrays.asList((Player[]) onlinePlayers);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return Bukkit.getOnlinePlayers();
        }
    }

}

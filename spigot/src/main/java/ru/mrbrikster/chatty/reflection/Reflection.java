package ru.mrbrikster.chatty.reflection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.util.Debugger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("all")
public class Reflection {

    private static MethodHandle getOnlinePlayersMethod;

    static {
        try {
            Chatty.instance().getExact(Debugger.class).debug("Choose modern getOnlinePlayers method.");

            getOnlinePlayersMethod = MethodHandles.lookup()
                    .findStatic(Bukkit.class, "getOnlinePlayers", MethodType.methodType(Collection.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            try {
                Chatty.instance().getExact(Debugger.class).debug("Choose outdated getOnlinePlayers method.");

                getOnlinePlayersMethod = MethodHandles.lookup()
                        .findStatic(Bukkit.class, "getOnlinePlayers", MethodType.methodType(Player[].class));
            } catch (NoSuchMethodException | IllegalAccessException e1) {
                e1.printStackTrace();
                Chatty.instance().getExact(Debugger.class).debug("Unable to choose getOnlinePlayers method.");
            }
        }
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            Object onlinePlayers = getOnlinePlayersMethod.invoke();
            if (onlinePlayers instanceof Collection) {
                return (Collection<? extends Player>) onlinePlayers;
            } else return Arrays.asList((Player[]) onlinePlayers);
        } catch (Throwable e) {
            return Bukkit.getOnlinePlayers();
        }
    }

}

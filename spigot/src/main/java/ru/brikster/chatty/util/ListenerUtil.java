package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@UtilityClass
public class ListenerUtil {

    public void unregister(Class<?> eventClazz, Plugin plugin) {
        try {
            HandlerList handlerList = (HandlerList) MethodHandles.publicLookup()
                    .findStatic(eventClazz, "getHandlerList", MethodType.methodType(HandlerList.class))
                    .invoke();
            handlerList.unregister(plugin);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

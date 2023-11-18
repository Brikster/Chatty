package ru.brikster.chatty.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Duration;

@UtilityClass
public class ListenerUtil {

    private final static Cache<Class<?>, MethodHandle> EVENT_CLASS_METHOD_HANDLE_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    public void unregister(Class<?> eventClazz, Plugin plugin) {
        try {
            MethodHandle methodHandle = EVENT_CLASS_METHOD_HANDLE_CACHE.get(eventClazz, () -> MethodHandles.publicLookup()
                    .findStatic(eventClazz, "getHandlerList", MethodType.methodType(HandlerList.class)));

            HandlerList handlerList = (HandlerList) methodHandle.invoke();
            handlerList.unregister(plugin);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

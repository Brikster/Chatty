package ru.brikster.chatty.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@UtilityClass
public class EventUtil {

    private final static Cache<Class<?>, MethodHandle> EVENT_CLASS_METHOD_HANDLE_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    @SneakyThrows
    public void callAsynchronously(Plugin plugin, Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    public void callSynchronously(Plugin plugin, Runnable runnable) {
        callSynchronously(plugin, () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T callSynchronously(Plugin plugin, Supplier<T> supplier) {
        if (Bukkit.isPrimaryThread()) {
            return supplier.get();
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future.join();
    }

    public void unregisterListeners(Class<?> eventClass, Plugin plugin) {
        try {
            MethodHandle methodHandle = EVENT_CLASS_METHOD_HANDLE_CACHE.get(eventClass, () -> MethodHandles.publicLookup()
                    .findStatic(eventClass, "getHandlerList", MethodType.methodType(HandlerList.class)));

            HandlerList handlerList = (HandlerList) methodHandle.invoke();
            handlerList.unregister(plugin);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

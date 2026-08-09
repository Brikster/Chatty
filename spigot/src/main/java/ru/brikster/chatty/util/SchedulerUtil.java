package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

@UtilityClass
public class SchedulerUtil {

    private static final MethodHandle GET_SCHEDULER;
    private static final MethodHandle RUN;
    private static final MethodHandle RUN_DELAYED;

    static {
        MethodHandle getScheduler = null;
        MethodHandle run = null;
        MethodHandle runDelayed = null;

        if (PaperUtil.isFolia()) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                Class<?> taskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

                getScheduler = lookup.findVirtual(Player.class, "getScheduler",
                        MethodType.methodType(schedulerClass));
                run = lookup.findVirtual(schedulerClass, "run",
                        MethodType.methodType(taskClass, Plugin.class, Consumer.class, Runnable.class));
                runDelayed = lookup.findVirtual(schedulerClass, "runDelayed",
                        MethodType.methodType(taskClass, Plugin.class, Consumer.class, Runnable.class, long.class));
            } catch (Throwable t) {
                getScheduler = null;
                run = null;
                runDelayed = null;
            }
        }

        GET_SCHEDULER = getScheduler;
        RUN = run;
        RUN_DELAYED = runDelayed;
    }

    public void runForPlayer(Plugin plugin, Player player, Runnable task) {
        if (GET_SCHEDULER == null) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        try {
            Object scheduler = GET_SCHEDULER.invoke(player);
            RUN.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run(), (Runnable) null);
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot schedule a task for " + player.getName(), t);
        }
    }

    public void runForPlayerLater(Plugin plugin, Player player, Runnable task, long delayTicks) {
        if (GET_SCHEDULER == null) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
            return;
        }
        try {
            Object scheduler = GET_SCHEDULER.invoke(player);
            RUN_DELAYED.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run(),
                    (Runnable) null, delayTicks);
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot schedule a delayed task for " + player.getName(), t);
        }
    }

}

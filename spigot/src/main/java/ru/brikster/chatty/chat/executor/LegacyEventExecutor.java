package ru.brikster.chatty.chat.executor;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public final class LegacyEventExecutor extends AbstractChatEventExecutor implements EventExecutor {

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (listener == this && event instanceof AsyncPlayerChatEvent) {
            AsyncPlayerChatEvent chatEvent = (AsyncPlayerChatEvent) event;
            handleEarly(new LegacyChatEventFacade(chatEvent), System.identityHashCode(chatEvent));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleFinishedEarlyContextEvent(AsyncPlayerChatEvent event) {
        handleLate(new LegacyChatEventFacade(event), System.identityHashCode(event));
    }

}

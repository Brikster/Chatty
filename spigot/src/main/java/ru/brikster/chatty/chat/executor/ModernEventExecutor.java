package ru.brikster.chatty.chat.executor;

import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;

public final class ModernEventExecutor extends AbstractChatEventExecutor {

    private static final String EVENT_CLASS_NAME = "io.papermc.paper.event.player.AsyncChatEvent";

    public static boolean isSupported() {
        try {
            Class.forName(EVENT_CLASS_NAME);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Class<? extends Event> getEventClass() throws ClassNotFoundException {
        return Class.forName(EVENT_CLASS_NAME).asSubclass(Event.class);
    }

    public void prepare() {
        ModernChatEventFacade.verifyAdaptable();
    }

    public EventExecutor earlyExecutor() {
        return (listener, event) -> handleEarly(facade(event), System.identityHashCode(event));
    }

    public EventExecutor lateExecutor() {
        return (listener, event) -> handleLate(facade(event), System.identityHashCode(event));
    }

    private ChatEventFacade facade(Event event) {
        return new ModernChatEventFacade(event, audiences);
    }

}

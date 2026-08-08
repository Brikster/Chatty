package ru.brikster.chatty.chat.executor;

import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;

public final class ModernEventExecutor extends AbstractChatEventExecutor {

    public static boolean isSupported() {
        return ModernChatEventFacade.isSupported();
    }

    public static Class<? extends Event> getEventClass() throws ClassNotFoundException {
        return Class.forName(ModernChatEventFacade.EVENT_CLASS_NAME).asSubclass(Event.class);
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

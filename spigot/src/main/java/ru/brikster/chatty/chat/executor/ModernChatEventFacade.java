package ru.brikster.chatty.chat.executor;

import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import ru.brikster.chatty.adventure.NativeAudienceAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class ModernChatEventFacade implements ChatEventFacade {

    private static final String ABSTRACT_EVENT_CLASS_NAME = "io.papermc.paper.event.player.AbstractChatEvent";

    private static final MethodHandle VIEWERS_METHOD;
    private static final MethodHandle MESSAGE_METHOD;
    private static final MethodHandle SET_MESSAGE_METHOD;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> abstractChatEvent = Class.forName(ABSTRACT_EVENT_CLASS_NAME);
            Class<?> componentClass = NativeAudienceAdapter.nativeComponentClass();

            VIEWERS_METHOD = lookup.findVirtual(abstractChatEvent, "viewers",
                    MethodType.methodType(Set.class));
            MESSAGE_METHOD = lookup.findVirtual(abstractChatEvent, "message",
                    MethodType.methodType(componentClass));
            SET_MESSAGE_METHOD = lookup.findVirtual(abstractChatEvent, "message",
                    MethodType.methodType(void.class, componentClass));
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot adapt " + ABSTRACT_EVENT_CLASS_NAME, t);
        }
    }

    static void verifyAdaptable() {
        Objects.requireNonNull(VIEWERS_METHOD);
    }

    private final Event event;
    private final BukkitAudiences audiences;

    ModernChatEventFacade(Event event, BukkitAudiences audiences) {
        this.event = event;
        this.audiences = audiences;
    }

    @Override
    public Player getPlayer() {
        return ((PlayerEvent) event).getPlayer();
    }

    @SneakyThrows
    @Override
    public String getMessage() {
        Object nativeMessage = MESSAGE_METHOD.invoke(event);
        return PlainTextComponentSerializer.plainText()
                .serialize(NativeAudienceAdapter.fromNativeComponent(nativeMessage));
    }

    @SneakyThrows
    @Override
    public void setMessage(String message) {
        SET_MESSAGE_METHOD.invoke(event,
                NativeAudienceAdapter.toNativeComponent(Component.text(message)));
    }

    @Override
    public List<Player> getRecipients() {
        List<Player> recipients = new ArrayList<>();
        for (Object viewer : viewers()) {
            if (viewer instanceof Player) {
                recipients.add((Player) viewer);
            }
        }
        return recipients;
    }

    @Override
    public void setRecipients(Collection<? extends Player> recipients) {
        Set<Object> viewers = viewers();
        viewers.clear();
        viewers.addAll(recipients);
    }

    @Override
    public void clearRecipients() {
        viewers().clear();
    }

    @Override
    public boolean isCancelled() {
        return ((Cancellable) event).isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        ((Cancellable) event).setCancelled(cancelled);
    }

    @Override
    public void writeConsoleLine(String line) {
        audiences.console().sendMessage(LegacyComponentSerializer.legacySection().deserialize(line));
    }

    @Override
    public String getDebugFormat() {
        return getMessage();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Set<Object> viewers() {
        Object viewers = VIEWERS_METHOD.invoke(event);
        return (Set<Object>) viewers;
    }

}

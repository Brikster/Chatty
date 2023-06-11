package ru.brikster.chatty.api.chat.handle.context;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Collection;
import java.util.Optional;

public interface MessageContext<T> {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

    @NotNull
    Component getFormat();

    void setFormat(@NotNull Component component);

    @NotNull
    Collection<? extends @NotNull Player> getRecipients();

    void setRecipients(@NotNull Collection<? extends @NotNull Player> recipients);

    @NotNull
    T getMessage();

    void setMessage(@NotNull T message);

    @NotNull
    Chat getChat();

    @NotNull
    Player getSender();

    <V> MessageContext<T> withTag(Tag<V> tag, @NotNull V value);

    <V> boolean hasTag(Tag<V> tag);

    <V> Optional<V> getTag(Tag<V> tag);

    <V> void removeTag(Tag<V> tag);

    class Tag<T> {

        private final String key;
        private final Class<T> clazz;

        private Tag(String key, Class<T> clazz) {
            this.key = key;
            this.clazz = clazz;
        }

        public String getKey() {
            return key;
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public static Tag<String> String(String key) {
            return new Tag<>(key, String.class);
        }

        public static Tag<Integer> Integer(String key) {
            return new Tag<>(key, Integer.class);
        }

        public static Tag<Boolean> Boolean(String key) {
            return new Tag<>(key, Boolean.class);
        }

    }

}

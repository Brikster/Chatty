package ru.brikster.chatty.chat.message.context;

import lombok.*;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

import java.util.*;

@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public final class MessageContextImpl<T> implements MessageContext<T> {

    @Getter
    private final @Nullable Chat chat;
    @Getter
    private final @Nullable Player sender;
    @Getter @Setter
    private boolean cancelled;
    @Getter @Setter
    private @NotNull Component format;
    @Getter @Setter
    private @NotNull Collection<? extends @NotNull Player> recipients;
    @Getter @Setter
    private @NotNull T message;

    private final @NotNull Map<String, Object> tagData = new HashMap<>();

    public MessageContextImpl(MessageContext<T> context) {
        this.cancelled = context.isCancelled();
        this.format = context.getFormat();
        this.recipients = new ArrayList<>(context.getRecipients());
        this.message = context.getMessage();
        this.chat = context.getChat();
        this.sender = context.getSender();
    }

    @Override
    public <V> MessageContextImpl<T> withTag(Tag<V> tag, @NotNull V value) {
        tagData.put(tag.getKey(), value);
        return this;
    }

    @Override
    public <V> boolean hasTag(Tag<V> tag) {
        String key = tag.getKey();
        return tagData.containsKey(key) && tagData.get(key).getClass() == tag.getClazz();
    }

    @Override
    public <V> Optional<V> getTag(Tag<V> tag) {
        return hasTag(tag) ? Optional.of((V) tagData.get(tag.getKey())) : Optional.empty();
    }

    @Override
    public <V> void removeTag(Tag<V> tag) {
        if (hasTag(tag)) {
            tagData.remove(tag.getKey());
        }
    }

}

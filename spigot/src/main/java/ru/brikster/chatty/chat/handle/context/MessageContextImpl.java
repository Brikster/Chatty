package ru.brikster.chatty.chat.handle.context;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@RequiredArgsConstructor
@AllArgsConstructor
public class MessageContextImpl<T> implements MessageContext<T> {

    @Getter @Setter
    private boolean cancelled;

    @Getter @Setter
    private @NotNull Component format;

    @Getter @Setter
    private @NotNull Collection<? extends @NotNull Player> recipients;

    @Getter @Setter
    private @NotNull T message;

    @Getter
    private final @Nullable Chat chat;

    @Getter
    private final @Nullable Player sender;

    public MessageContextImpl(MessageContext<T> context) {
        this.cancelled = context.isCancelled();
        this.format = context.getFormat();
        this.recipients = context.getRecipients();
        this.message = context.getMessage();
        this.chat = context.getChat();
        this.sender = context.getSender();
    }

}

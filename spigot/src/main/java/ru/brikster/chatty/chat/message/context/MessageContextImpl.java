package ru.brikster.chatty.chat.message.context;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class MessageContextImpl<T> implements MessageContext<T> {

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

    public MessageContextImpl(MessageContext<T> context) {
        this.cancelled = context.isCancelled();
        this.format = context.getFormat();
        this.recipients = new ArrayList<>(context.getRecipients());
        this.message = context.getMessage();
        this.chat = context.getChat();
        this.sender = context.getSender();
    }

}

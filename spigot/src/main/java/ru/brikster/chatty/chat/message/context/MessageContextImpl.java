package ru.brikster.chatty.chat.message.context;

import lombok.*;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.message.context.MessageContext;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public final class MessageContextImpl<MessageT> implements MessageContext<MessageT> {

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
    private MessageT message;
    @Getter @Setter
    private @Nullable Player target;

    public MessageContextImpl(MessageContext<?> context) {
        this.cancelled = context.isCancelled();
        this.format = context.getFormat();
        this.recipients = new ArrayList<>(context.getRecipients());
        this.chat = context.getChat();
        this.sender = context.getSender();
        this.target = context.getTarget();
    }

}

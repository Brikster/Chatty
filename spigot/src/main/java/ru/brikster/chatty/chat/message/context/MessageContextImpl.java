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
import java.util.Map;

@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Getter @Setter
public final class MessageContextImpl<MessageT> implements MessageContext<MessageT> {

    private final @Nullable Chat chat;
    private final @Nullable Player sender;
    private final @NotNull Map<String, Object> metadata;
    private boolean cancelled;
    private @NotNull Component format;
    private @NotNull Collection<? extends @NotNull Player> recipients;
    private MessageT message;
    private @Nullable Player target;

    public MessageContextImpl(MessageContext<?> context) {
        this.cancelled = context.isCancelled();
        this.format = context.getFormat();
        this.recipients = new ArrayList<>(context.getRecipients());
        this.chat = context.getChat();
        this.sender = context.getSender();
        this.metadata = context.getMetadata();
        this.target = context.getTarget();
    }

}

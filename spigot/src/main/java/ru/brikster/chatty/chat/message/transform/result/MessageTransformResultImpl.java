package ru.brikster.chatty.chat.message.transform.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

import java.util.Collection;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class MessageTransformResultImpl<MessageT> implements MessageTransformResult<MessageT> {

    @NotNull MessageContext<MessageT> newContext;

    Collection<? extends @NotNull Player> removedRecipients;

    boolean formatUpdated;
    boolean messageUpdated;
    boolean becameCancelled;

    public MessageTransformResultImpl(@NotNull MessageContext<MessageT> newContext,
                                      Collection<? extends @NotNull Player> removedRecipients,
                                      boolean formatUpdated, boolean messageUpdated, boolean becameCancelled) {
        this.newContext = newContext;
        this.removedRecipients = removedRecipients;
        this.formatUpdated = formatUpdated;
        this.messageUpdated = messageUpdated;
        this.becameCancelled = becameCancelled;
    }

}

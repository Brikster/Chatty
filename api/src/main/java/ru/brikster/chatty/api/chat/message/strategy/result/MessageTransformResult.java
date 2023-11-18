package ru.brikster.chatty.api.chat.message.strategy.result;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;

import java.util.Collection;

public interface MessageTransformResult<MessageT> {

    default boolean wasUpdated() {
        return !getRemovedRecipients().isEmpty()
                || isFormatUpdated()
                || isMessageUpdated()
                || isBecameCancelled();
    }

    @NotNull MessageContext<MessageT> getNewContext();

    @NotNull
    Collection<? extends @NotNull Player> getRemovedRecipients();
    @NotNull
    Collection<? extends @NotNull Player> getAddedRecipients();

    boolean isFormatUpdated();

    boolean isMessageUpdated();

    boolean isBecameCancelled();

}

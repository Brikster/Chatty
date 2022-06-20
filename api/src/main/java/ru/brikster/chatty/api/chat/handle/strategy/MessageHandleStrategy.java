package ru.brikster.chatty.api.chat.handle.strategy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

import java.util.Collection;

public interface MessageHandleStrategy<F, T> {

    Result<T> handle(MessageContext<F> context);

    interface Result<T> {

        default boolean wasUpdated() {
            return getAddedRecipients().size() != 0
                    || getRemovedRecipients().size() != 0
                    || isFormatUpdated()
                    || isMessageUpdated()
                    || isBecameCancelled()
                    || isBecameUncancelled();
        }

        @NotNull MessageContext<T> getNewContext();

        @NotNull
        Collection<? extends @NotNull Player> getAddedRecipients();

        @NotNull
        Collection<? extends @NotNull Player> getRemovedRecipients();

        boolean isFormatUpdated();

        boolean isMessageUpdated();

        boolean isBecameCancelled();

        boolean isBecameUncancelled();

    }

}

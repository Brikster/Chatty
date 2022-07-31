package ru.brikster.chatty.chat.handle.strategy.general;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneralMessageTransformStrategy<T> implements MessageTransformStrategy<String, T> {

    @Override
    public @NotNull Result<T> handle(MessageContext<String> context) {
        MessageContext<?> newContext = new MessageContextImpl<>(context);

        List<Player> addedRecipients = new ArrayList<>();
        List<Player> removedRecipients = new ArrayList<>();

        boolean formatUpdated = false;
        boolean messageUpdated = false;
        boolean becameCancelled = false;
        boolean becameUncancelled = false;

        for (MessageTransformStrategy<?, ?> strategy : context.getChat().getStrategies()) {
            if (strategy.getStage() != getStage()) {
                continue;
            }

            try {
                Result<?> result = ((MessageTransformStrategy) strategy).handle(newContext);

                newContext = result.getNewContext();
                formatUpdated |= result.isFormatUpdated();
                messageUpdated |= result.isMessageUpdated();
                becameCancelled |= result.isBecameCancelled();
                becameUncancelled |= result.isBecameUncancelled();

                addedRecipients.addAll(result.getAddedRecipients());
                removedRecipients.removeAll(result.getAddedRecipients());

                removedRecipients.addAll(result.getRemovedRecipients());
                addedRecipients.removeAll(result.getRemovedRecipients());
            } catch (ClassCastException t) {
                throw new IllegalArgumentException("Illegal order in strategies chain", t);
            }
        }

        if (!getStage().getFinalTransformClass().isAssignableFrom(newContext.getMessage().getClass())) {
            throw new IllegalArgumentException("Strategies chain should end with string context");
        }

        return ResultImpl
                .<T>builder()
                .newContext((MessageContext<T>) newContext)
                .messageUpdated(messageUpdated)
                .formatUpdated(formatUpdated)
                .becameUncancelled(becameUncancelled)
                .becameCancelled(becameCancelled)
                .addedRecipients(addedRecipients)
                .removedRecipients(removedRecipients)
                .build();
    }

}

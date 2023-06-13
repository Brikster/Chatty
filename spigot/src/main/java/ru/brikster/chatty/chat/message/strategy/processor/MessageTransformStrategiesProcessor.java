package ru.brikster.chatty.chat.message.strategy.processor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy.Result;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy.Stage;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class MessageTransformStrategiesProcessor<FromT, IntoT> {

    public @NotNull Result<IntoT> handle(MessageContext<FromT> context) {
        MessageContext<?> newContext = new MessageContextImpl<>(context);

        List<Player> addedRecipients = new ArrayList<>();
        List<Player> removedRecipients = new ArrayList<>();

        boolean formatUpdated = false;
        boolean messageUpdated = false;
        boolean becameCancelled = false;

        for (MessageTransformStrategy<?, ?> strategy : context.getChat().getStrategies()) {
            if (strategy.getStage() != getStage()) {
                continue;
            }

            try {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Result<?> result = ((MessageTransformStrategy) strategy).handle(newContext);

                newContext = result.getNewContext();
                formatUpdated |= result.isFormatUpdated();
                messageUpdated |= result.isMessageUpdated();
                becameCancelled |= result.isBecameCancelled();

                addedRecipients.addAll(result.getAddedRecipients());
                removedRecipients.removeAll(result.getAddedRecipients());

                removedRecipients.addAll(result.getRemovedRecipients());
                addedRecipients.removeAll(result.getRemovedRecipients());
            } catch (ClassCastException t) {
                throw new IllegalArgumentException("Illegal order in strategies chain", t);
            }
        }

        if (!getStage().getTargetTransformClass().isAssignableFrom(newContext.getMessage().getClass())) {
            throw new IllegalArgumentException("Strategies chain should end with string context");
        }

        //noinspection unchecked
        return ResultImpl
                .<IntoT>builder()
                .newContext((MessageContext<IntoT>) newContext)
                .messageUpdated(messageUpdated)
                .formatUpdated(formatUpdated)
                .becameCancelled(becameCancelled)
                .addedRecipients(addedRecipients)
                .removedRecipients(removedRecipients)
                .build();
    }

    public abstract Stage getStage();

}

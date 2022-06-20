package ru.brikster.chatty.chat.handle.strategy.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageHandleStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;

import java.util.ArrayList;
import java.util.List;

public class GeneralMessageHandleStrategy implements MessageHandleStrategy<String, Component> {

    @Override
    public Result<Component> handle(MessageContext<String> context) {
        MessageContext<?> newContext = new MessageContextImpl<>(context);

        List<Player> addedRecipients = new ArrayList<>();
        List<Player> removedRecipients = new ArrayList<>();

        boolean formatUpdated = false;
        boolean messageUpdated = false;
        boolean becameCancelled = false;
        boolean becameUncancelled = false;

        for (MessageHandleStrategy<?, ?> strategy : context.getChat().getStrategies()) {
            try {
                Result<?> result = ((MessageHandleStrategy) strategy).handle(newContext);

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

        if (!Component.class.isAssignableFrom(newContext.getMessage().getClass())) {
            throw new IllegalArgumentException("Strategies chain should end with Component context");
        }

        return ResultImpl
                .<Component>builder()
                .newContext((MessageContext<Component>) newContext)
                .messageUpdated(messageUpdated)
                .formatUpdated(formatUpdated)
                .becameUncancelled(becameUncancelled)
                .becameCancelled(becameCancelled)
                .addedRecipients(addedRecipients)
                .removedRecipients(removedRecipients)
                .build();
    }

}

package ru.brikster.chatty.chat.message.transform.processor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.Stage;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.TransformRule;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public final class MessageTransformStrategiesProcessorImpl implements MessageTransformStrategiesProcessor {

    @Inject
    private Set<MessageTransformStrategy<?>> strategies;

    @Override
    public <MessageT> @NotNull MessageTransformResult<MessageT> handle(MessageContext<MessageT> context, Stage stage) {
        MessageContext<?> newContext = context;

        List<Player> removedRecipients = new ArrayList<>();
        List<Player> addedRecipients = new ArrayList<>();

        boolean formatUpdated = false;
        boolean messageUpdated = false;
        boolean becameCancelled = false;

        List<MessageTransformStrategy<?>> strategies = Stream.concat(this.strategies.stream(), context.getChat().getStrategies().stream())
                .filter(strategy -> strategy.getStage() == stage)
                .collect(Collectors.toList());

        for (MessageTransformStrategy<?> strategy : strategies) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            MessageTransformResult<?> messageTransformResult = ((MessageTransformStrategy) strategy).handle(newContext);

            newContext = messageTransformResult.getNewContext();
            formatUpdated |= messageTransformResult.isFormatUpdated();
            messageUpdated |= messageTransformResult.isMessageUpdated();
            becameCancelled |= messageTransformResult.isBecameCancelled();

            removedRecipients.addAll(messageTransformResult.getRemovedRecipients());
            addedRecipients.addAll(messageTransformResult.getAddedRecipients());
        }

        if (becameCancelled && stage.hasRule(TransformRule.DENY_CANCEL)) {
            throw new IllegalStateException("Strategy at stage " + stage + " cannot cancel message");
        }

        if (formatUpdated && stage.hasRule(TransformRule.DENY_FORMAT_UPDATE)) {
            throw new IllegalStateException("Strategy at stage " + stage + " cannot update format");
        }

        if ((!removedRecipients.isEmpty() || !addedRecipients.isEmpty())
                && stage.hasRule(TransformRule.DENY_UPDATE_RECIPIENTS)) {
            throw new IllegalStateException("Strategy at stage " + stage + " cannot update recipients");
        }

        //noinspection unchecked
        return new MessageTransformResultImpl<>((MessageContext<MessageT>) newContext,
                removedRecipients,
                addedRecipients,
                formatUpdated, messageUpdated, becameCancelled);
    }

}

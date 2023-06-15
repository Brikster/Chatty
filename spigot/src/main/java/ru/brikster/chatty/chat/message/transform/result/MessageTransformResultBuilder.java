package ru.brikster.chatty.chat.message.transform.result;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

import java.util.Collection;

public interface MessageTransformResultBuilder<MessageT> {

    @Contract(value = "_ -> this")
    MessageTransformResultBuilder<MessageT> withRecipients(Collection<Player> recipients);

    @Contract(value = "_ -> this")
    MessageTransformResultBuilder<MessageT> withFormat(Component format);

    @Contract(value = "_ -> this")
    MessageTransformResultBuilder<MessageT> withMessage(MessageT message);

    @Contract(value = "-> this")
    MessageTransformResultBuilder<MessageT> withCancelled();

    MessageTransformResult<MessageT> build();

    static <MessageT> MessageTransformResultBuilder<MessageT> fromContext(MessageContext<?> oldContext) {
        return new MessageTransformResultBuilderImpl<>(oldContext);
    }

}

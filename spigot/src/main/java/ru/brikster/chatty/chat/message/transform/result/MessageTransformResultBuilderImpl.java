package ru.brikster.chatty.chat.message.transform.result;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;

import java.util.*;
import java.util.stream.Stream;

final class MessageTransformResultBuilderImpl<MessageT> implements MessageTransformResultBuilder<MessageT> {

    private final Map<String, Object> metadata = new HashMap<>();

    private final MessageContext<?> oldContext;
    private Collection<Player> recipients;
    private Component format;
    private MessageT message;
    private boolean cancel;

    public MessageTransformResultBuilderImpl(MessageContext<?> oldContext) {
        this.oldContext = oldContext;
    }

    @Override
    @Contract(value = "_ -> this")
    public MessageTransformResultBuilderImpl<MessageT> withRecipients(Collection<Player> recipients) {
        this.recipients = recipients;
        return this;
    }

    @Override
    @Contract(value = "_ -> this")
    public MessageTransformResultBuilderImpl<MessageT> withFormat(Component format) {
        this.format = format;
        return this;
    }

    @Override
    @Contract(value = "_ -> this")
    public MessageTransformResultBuilderImpl<MessageT> withMessage(MessageT message) {
        this.message = message;
        return this;
    }

    @Override
    @Contract(value = "-> this")
    public MessageTransformResultBuilderImpl<MessageT> withCancelled() {
        this.cancel = true;
        return this;
    }

    @Contract(value = "_, _ -> this")
    public MessageTransformResultBuilder<MessageT> withMetadata(@NotNull String key, @Nullable Object value) {
        this.metadata.put(key, value);
        return this;
    }

    @Override
    public MessageTransformResult<MessageT> build() {
        MessageContext<MessageT> newContext = new MessageContextImpl<>(oldContext);

        boolean formatUpdated = false;
        if (format != null) {
            formatUpdated = !Objects.equals(format, oldContext.getFormat());
            newContext.setFormat(format);
        }

        Collection<Player> removedRecipients = new HashSet<>();
        Collection<Player> addedRecipients = new HashSet<>();
        if (recipients != null) {
            Stream.concat(recipients.stream(), oldContext.getRecipients().stream())
                    .distinct()
                    .forEach(recipient -> {
                        boolean inNewCollection = recipients.contains(recipient);
                        boolean inOldCollection = oldContext.getRecipients().contains(recipient);
                        if (inNewCollection) {
                            if (!inOldCollection) {
                                addedRecipients.add(recipient);
                            }
                        } else {
                            if (inOldCollection) {
                                removedRecipients.add(recipient);
                            }
                        }
                    });
            newContext.setRecipients(recipients);
        }

        boolean messageUpdated = false;
        if (message != null) {
            messageUpdated = !Objects.equals(message, oldContext.getMessage());
            newContext.setMessage(message);
        } else {
            //noinspection unchecked
            newContext.setMessage((MessageT) oldContext.getMessage());
        }

        boolean becomeCancelled = false;
        if (cancel) {
            becomeCancelled = !oldContext.isCancelled();
            newContext.setCancelled(true);
        }

        newContext.getMetadata().putAll(metadata);

        return new MessageTransformResultImpl<>(newContext,
                removedRecipients,
                addedRecipients,
                formatUpdated, messageUpdated, becomeCancelled);
    }

}

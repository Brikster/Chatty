package ru.brikster.chatty.api.chat.message.strategy;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

import static ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.TransformRule.*;

public interface MessageTransformStrategy<MessageT> {

    @NotNull MessageTransformResult<MessageT> handle(MessageContext<MessageT> context);

    @NotNull Stage getStage();

    // TODO revert to allowed actions
    enum TransformRule {
        DENY_CANCEL,
        DENY_FORMAT_UPDATE,
        DENY_UPDATE_RECIPIENTS
    }

    @Getter
    enum Stage {
        // Ungrouped stage with string message
        EARLY(String.class, DENY_FORMAT_UPDATE),
        // Ungrouped stage with component message
        MIDDLE(Component.class, DENY_CANCEL, DENY_FORMAT_UPDATE),
        // Grouped stage with component message
        LATE(Component.class, DENY_CANCEL, DENY_UPDATE_RECIPIENTS),
        // Personal stage with component message
        POST(Component.class, DENY_CANCEL, DENY_UPDATE_RECIPIENTS);

        private final Class<?> messageType;
        private final TransformRule[] rules;

        Stage(Class<?> messageType, TransformRule... rules) {
            this.messageType = messageType;
            this.rules = rules;
        }

        public boolean hasRule(TransformRule rule) {
            for (TransformRule transformRule : rules) {
                if (transformRule == rule) return true;
            }
            return false;
        }
    }

}

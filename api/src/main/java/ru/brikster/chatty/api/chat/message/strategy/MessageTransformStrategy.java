package ru.brikster.chatty.api.chat.message.strategy;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

import static ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.TransformRule.*;

public interface MessageTransformStrategy<MessageT> {

    @NotNull MessageTransformResult<MessageT> handle(MessageContext<MessageT> context, @Nullable Player target);

    @NotNull Stage getStage();

    enum TransformRule {
        DENY_CANCEL,
        DENY_FORMAT_UPDATE,
        DENY_REMOVE_RECIPIENTS
    }

    enum Stage {
        EARLY(String.class, DENY_FORMAT_UPDATE),
        LATE(String.class, DENY_CANCEL),
        POST(Component.class, DENY_CANCEL, DENY_REMOVE_RECIPIENTS);

        private final Class<?> messageType;
        private final TransformRule[] rules;

        Stage(Class<?> messageType, TransformRule... rules) {
            this.messageType = messageType;
            this.rules = rules;
        }

        public Class<?> getMessageType() {
            return messageType;
        }

        public TransformRule[] getRules() {
            return rules;
        }

        public boolean hasRule(TransformRule rule) {
            for (TransformRule transformRule : rules) {
                if (transformRule == rule) return true;
            }
            return false;
        }
    }

}

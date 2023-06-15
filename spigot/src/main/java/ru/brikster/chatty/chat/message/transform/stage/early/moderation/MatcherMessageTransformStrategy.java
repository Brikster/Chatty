package ru.brikster.chatty.chat.message.transform.stage.early.moderation;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.chat.message.transform.stage.EarlyMessageTransformStrategy;

public interface MatcherMessageTransformStrategy extends EarlyMessageTransformStrategy {


    default MessageTransformResult<String> getMatcherResult(MessageContext<String> context,
                                                            String matchedMessage,
                                                            boolean messageMatches,
                                                            boolean useBlock) {
        if (messageMatches) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        } else {
            if (useBlock) {
                return MessageTransformResultBuilder.<String>fromContext(context)
                        .withMessage(matchedMessage)
                        .withCancelled()
                        .build();
            } else {
                return MessageTransformResultBuilder.<String>fromContext(context)
                        .withMessage(matchedMessage)
                        .build();
            }
        }
    }

    @Override
    default @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

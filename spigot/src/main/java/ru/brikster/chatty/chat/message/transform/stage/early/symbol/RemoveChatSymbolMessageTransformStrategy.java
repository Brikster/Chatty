package ru.brikster.chatty.chat.message.transform.stage.early.symbol;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.chat.message.transform.stage.EarlyMessageTransformStrategy;

import java.util.regex.Pattern;

public final class RemoveChatSymbolMessageTransformStrategy implements EarlyMessageTransformStrategy {

    @Override
    public @NotNull MessageTransformResult<String> handle(final MessageContext<String> context) {
        if (context.getChat().getSymbol().isEmpty()) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        } else {
            String message = context.getMessage()
                    .replaceFirst(Pattern.quote(context.getChat().getSymbol()), "");
            return MessageTransformResultBuilder.<String>fromContext(context)
                    .withMessage(message)
                    .build();
        }
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}
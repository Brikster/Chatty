package ru.brikster.chatty.chat.message.strategy.impl;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;

import java.util.regex.Pattern;

public final class RemoveChatSymbolMessageTransformStrategy implements MessageTransformStrategy<String, String> {

    private static final RemoveChatSymbolMessageTransformStrategy INSTANCE = new RemoveChatSymbolMessageTransformStrategy();

    @Override
    public @NotNull Result<String> handle(final MessageContext<String> context) {
        String message = context.getMessage();

        if (context.getChat().getSymbol().isEmpty()) {
            MessageContext<String> newContext = new MessageContextImpl<>(context);
            return ResultImpl.<String>builder()
                    .newContext(newContext)
                    .build();
        } else {
            message = message.replaceFirst(Pattern.quote(context.getChat().getSymbol()), "");
            MessageContext<String> newContext = new MessageContextImpl<>(context);
            newContext.setMessage(message);
            return ResultImpl.<String>builder()
                    .newContext(newContext)
                    .messageUpdated(true)
                    .build();
        }
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    public static RemoveChatSymbolMessageTransformStrategy instance() {
        return INSTANCE;
    }

}

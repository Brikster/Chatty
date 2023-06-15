package ru.brikster.chatty.chat.message.transform.processor;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.Stage;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

public interface MessageTransformStrategiesProcessor {

    <MessageT> @NotNull MessageTransformResult<MessageT> handle(MessageContext<MessageT> context, Stage stage);

}

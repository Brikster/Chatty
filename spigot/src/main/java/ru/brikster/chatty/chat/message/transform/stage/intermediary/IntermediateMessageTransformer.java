package ru.brikster.chatty.chat.message.transform.stage.intermediary;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

public interface IntermediateMessageTransformer {

    @NotNull MessageTransformResult<Component> handle(MessageContext<String> context);

}

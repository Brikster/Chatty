package ru.brikster.chatty.chat.message.transform.stage;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

public interface EarlyMessageTransformStrategy extends MessageTransformStrategy<String> {

    @NotNull MessageTransformResult<String> handle(MessageContext<String> context);

    @Override
    default @NotNull MessageTransformResult<String> handle(MessageContext<String> context, @Nullable Player target) {
        return handle(context);
    }

}

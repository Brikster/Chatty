package ru.brikster.chatty.api.chat.message.strategy.stage;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;

public interface LateMessageTransformStrategy extends MessageTransformStrategy<String> {

    default @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    default @NotNull MessageTransformResult<String> handle(MessageContext<String> context, @Nullable Player target) {
        return handle(context);
    }

}

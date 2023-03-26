package ru.brikster.chatty.chat.message.strategy.general;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

/**
 * Used for strategies, that should be called for messages which cancelled state won't be updated
 */
public final class PostMessageTransformStrategy extends GeneralMessageTransformStrategy<Component> {

    @Override
    public @NotNull Result<Component> handle(final MessageContext<String> context) {
        Result<Component> result = super.handle(context);

        if (result.isBecameCancelled() && result.isBecameUncancelled()) {
            throw new IllegalStateException("Strategies at POST transform stage cannot switch cancelled state");
        }

        return result;
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.POST;
    }

}

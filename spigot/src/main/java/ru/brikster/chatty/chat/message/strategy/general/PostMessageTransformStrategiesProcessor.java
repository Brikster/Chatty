package ru.brikster.chatty.chat.message.strategy.general;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy.Result;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy.Stage;

/**
 * Used for strategies that should be called for messages which cancelled state won't be updated
 */
public final class PostMessageTransformStrategiesProcessor extends MessageTransformStrategiesProcessor<Component, Component> {

    @Override
    public @NotNull Result<Component> handle(final MessageContext<Component> context) {
        Result<Component> result = super.handle(context);

        if (result.isBecameCancelled()) {
            throw new IllegalStateException("Strategies at POST transform stage cannot switch cancelled state");
        }

        return result;
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.POST;
    }

}

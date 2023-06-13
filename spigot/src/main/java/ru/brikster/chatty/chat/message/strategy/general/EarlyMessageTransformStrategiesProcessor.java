package ru.brikster.chatty.chat.message.strategy.general;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy.Stage;

public final class EarlyMessageTransformStrategiesProcessor extends MessageTransformStrategiesProcessor<String, String> {

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

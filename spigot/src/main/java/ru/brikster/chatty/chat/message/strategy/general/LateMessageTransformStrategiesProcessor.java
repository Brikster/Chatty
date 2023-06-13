package ru.brikster.chatty.chat.message.strategy.general;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy.Stage;

public final class LateMessageTransformStrategiesProcessor extends MessageTransformStrategiesProcessor<String, Component> {

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

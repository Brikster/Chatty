package ru.brikster.chatty.chat.message.strategy.general;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class LateMessageTransformStrategy extends GeneralMessageTransformStrategy<Component> {

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

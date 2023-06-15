package ru.brikster.chatty.chat.component.impl;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;

public final class DummyPlaceholdersComponentTransformer extends PlaceholdersComponentTransformer {

    @Override
    public @NotNull String transform(@NotNull String serializedString, @NotNull SinglePlayerTransformContext transformContext) {
        return serializedString;
    }

}

package ru.brikster.chatty.chat.component.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DummyPlaceholdersComponentTransformer extends PlaceholdersComponentTransformer {

    private static final PlaceholdersComponentTransformer INSTANCE = new DummyPlaceholdersComponentTransformer();

    @Override
    public @NotNull String transform(@NotNull String serializedString, @NotNull SinglePlayerTransformContext transformContext) {
        return serializedString;
    }

    public static PlaceholdersComponentTransformer instance() {
        return INSTANCE;
    }

}

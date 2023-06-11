package ru.brikster.chatty.chat.component.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;

public final class PlaceholderApiComponentTransformer extends PlaceholdersComponentTransformer {

    private static final PlaceholderApiComponentTransformer INSTANCE = new PlaceholderApiComponentTransformer();

    private PlaceholderApiComponentTransformer() {}

    @Override
    public @NotNull String transform(@NotNull final String serializedString, @NotNull final SinglePlayerTransformContext transformContext) {
        return PlaceholderAPI.setPlaceholders(transformContext.getPlayer(), serializedString);
    }

    public static PlaceholderApiComponentTransformer instance() {
        return INSTANCE;
    }

}

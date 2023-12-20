package ru.brikster.chatty.chat.component.impl;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;

import java.util.List;

@RequiredArgsConstructor
public final class ChainPlaceholdersComponentTransformer implements PlaceholdersComponentTransformer {

    private final List<PlaceholdersComponentTransformer> transformerList;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        Component result = formatComponent;
        for (PlaceholdersComponentTransformer transformer : transformerList) {
            result = transformer.transform(result, context);
        }
        return result;
    }

}

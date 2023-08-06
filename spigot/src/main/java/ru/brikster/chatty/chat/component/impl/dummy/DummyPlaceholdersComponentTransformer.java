package ru.brikster.chatty.chat.component.impl.dummy;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;

import javax.inject.Singleton;

@Singleton
public final class DummyPlaceholdersComponentTransformer extends PlaceholdersComponentTransformer {

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        return formatComponent;
    }

}

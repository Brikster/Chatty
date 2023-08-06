package ru.brikster.chatty.chat.component.impl.dummy;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.TwoPlayersTransformContext;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;

import javax.inject.Singleton;

@Singleton
public final class DummyRelationalPlaceholdersComponentTransformer extends RelationalPlaceholdersComponentTransformer {

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull TwoPlayersTransformContext context) {
        return formatComponent;
    }

}
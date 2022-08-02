package ru.brikster.chatty.chat.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.TransformContext;

public interface ComponentTransformer<T extends TransformContext> {

    @NotNull Component transform(@NotNull Component formatComponent, @NotNull T context);

}

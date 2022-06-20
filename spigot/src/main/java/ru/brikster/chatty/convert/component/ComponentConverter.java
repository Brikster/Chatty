package ru.brikster.chatty.convert.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ComponentConverter {

    @NotNull
    Component convert(@NotNull String message);

}

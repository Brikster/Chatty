package ru.brikster.chatty.convert.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ComponentStringConverter {

    @NotNull
    Component stringToComponent(@NotNull String value);

    @NotNull
    String componentToString(@NotNull Component value);

}

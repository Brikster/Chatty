package ru.brikster.chatty.convert.message;

import org.jetbrains.annotations.NotNull;

public interface MessageConverter {

    @NotNull
    String convert(@NotNull String message);

}

package ru.brikster.chatty.command;

import cloud.commandframework.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public interface CommandSuggestionsProvider<T> extends BiFunction<CommandContext<T>, String, List<String>>  {

    @NotNull List<@NotNull String> provideSuggestions(@NotNull CommandContext<@NotNull T> commandContext,
                                                      @NotNull String arg);

    @Override
    default List<String> apply(CommandContext<T> commandContext, String arg) {
        return provideSuggestions(commandContext, arg);
    }

}

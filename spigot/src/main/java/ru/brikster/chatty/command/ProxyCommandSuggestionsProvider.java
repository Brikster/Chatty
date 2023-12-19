package ru.brikster.chatty.command;

import cloud.commandframework.context.CommandContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public final class ProxyCommandSuggestionsProvider<S> implements CommandSuggestionsProvider<S> {

    private CommandSuggestionsProvider<S> backendProvider;

    @Override
    public @NotNull List<@NotNull String> provideSuggestions(@NotNull CommandContext<@NotNull S> commandContext, @NotNull String arg) {
        return backendProvider.provideSuggestions(commandContext, arg);
    }

}

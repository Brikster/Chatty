package ru.brikster.chatty.command;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
@AllArgsConstructor
public final class ProxyCommandHandler<S> implements CommandExecutionHandler<S> {

    private CommandExecutionHandler<S> backendHandler;

    @Override
    public void execute(@NotNull CommandContext<S> commandContext) {
        backendHandler.execute(commandContext);
    }

}

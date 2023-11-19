package ru.brikster.chatty.command;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
@AllArgsConstructor
public final class ProxyCommandHandler<T, C> implements CommandExecutionHandler<C> {

    private CommandExecutionHandler<C> executionHandler;

    @Override
    public void execute(@NotNull CommandContext<C> commandContext) {
        executionHandler.execute(commandContext);
    }

}

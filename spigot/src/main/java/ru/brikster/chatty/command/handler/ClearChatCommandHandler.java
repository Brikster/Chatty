package ru.brikster.chatty.command.handler;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.config.file.MessagesConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public final class ClearChatCommandHandler implements CommandExecutionHandler<CommandSender> {

    private final BukkitAudiences audiences;
    private final MessagesConfig messagesConfig;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        var players = audiences.filter(sender -> !(sender instanceof ConsoleCommandSender));
        for (int i = 0; i < 30; i++) {
            players.sendMessage(Component.empty());
        }
        audiences
                .sender(commandContext.getSender())
                .sendMessage(messagesConfig.getClearchatCommandSuccess());
    }

}

package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.type.MessagesConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class MsgCommandHandler implements CommandExecutionHandler<CommandSender> {

    @Inject private PmMessageService pmMessageService;
    @Inject private PrivateMessageCommandHandler privateMessageCommandHandler;
    @Inject private BukkitAudiences audiences;
    @Inject private MessagesConfig messagesConfig;

    @Override
    public void execute(@NotNull CommandContext<CommandSender> commandContext) {
        CommandSender sender = commandContext.getSender();

        String targetName = commandContext.get("target");
        CommandSender target = pmMessageService.resolveTarget(targetName, true);
        if (target == null) {
            audiences.sender(sender).sendMessage(messagesConfig.getPmPlayerNotFound());
            return;
        }

        privateMessageCommandHandler.handleCommand(commandContext, sender, target);
    }

}

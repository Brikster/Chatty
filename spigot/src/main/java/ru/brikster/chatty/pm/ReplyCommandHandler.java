package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.pm.targets.PmMessageTarget;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ReplyCommandHandler implements CommandExecutionHandler<CommandSender> {

    @Inject private BukkitAudiences audiences;
    @Inject private PmMessageService pmMessageService;
    @Inject private MessagesConfig messagesConfig;
    @Inject private PrivateMessageCommandHandler privateMessageCommandHandler;

    @Override
    public void execute(@NotNull CommandContext<CommandSender> commandContext) {
        CommandSender sender = commandContext.getSender();
        PmMessageTarget target = pmMessageService.getLastConversation(sender);

        if (target == null) {
            audiences.sender(sender).sendMessage(messagesConfig.getPmNobodyToReply());
            return;
        }

        privateMessageCommandHandler.handleCommand(commandContext, sender, target);
    }

}

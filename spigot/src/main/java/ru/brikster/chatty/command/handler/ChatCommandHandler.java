package ru.brikster.chatty.command.handler;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelectionState;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public final class ChatCommandHandler implements CommandExecutionHandler<CommandSender> {

    private final ChatRegistry chatRegistry;
    private final ChatSelectionState selectionState;
    private final MessagesConfig messagesConfig;
    private final BukkitAudiences audiences;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        Player sender = (Player) commandContext.getSender();
        String chatId = commandContext.get("chat-id");

        Chat chat = chatRegistry.getChats().get(chatId);
        if (chat == null) {
            audiences.sender(sender).sendMessage(messagesConfig.getChatNotFound());
            return;
        }

        if (chat.isPermissionRequired() && !chat.hasCommandWritePermission(sender)) {
            audiences.sender(sender).sendMessage(messagesConfig.getCmdNoPermissionError());
            return;
        }

        Optional<String> message = commandContext.getOptional("message");
        if (message.isPresent()) {
            selectionState.sendNext(sender.getUniqueId(), chatId);
            try {
                sender.chat(message.get());
            } finally {
                selectionState.takePendingChat(sender.getUniqueId());
            }
            return;
        }

        if (chat.getCommand() == null || !chat.getCommand().isCanSwitchWithCommand()) {
            audiences.sender(sender).sendMessage(messagesConfig.getCmdUsageError()
                    .replaceText(AdventureUtil.createReplacement("{usage}",
                            "/" + chat.getCommand().getName() + " <message>")));
            return;
        }

        selectionState.switchTo(sender.getUniqueId(), chatId);
        audiences.sender(sender).sendMessage(messagesConfig.getChatCommandSwitched()
                .replaceText(AdventureUtil.createReplacement("{chat}", chat.getDisplayName())));
    }

}

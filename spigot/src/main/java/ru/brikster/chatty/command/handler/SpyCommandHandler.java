package ru.brikster.chatty.command.handler;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public final class SpyCommandHandler implements CommandExecutionHandler<CommandSender> {

    private final PlayerDataRepository playerDataRepository;
    private final MessagesConfig messagesConfig;
    private final BukkitAudiences audiences;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        boolean state = commandContext.get("state");
        Player sender = (Player) commandContext.getSender();
        playerDataRepository.createOrUpdateUser(sender.getUniqueId(), sender.getName());
        playerDataRepository.setEnableSpy(sender.getUniqueId(), state);
        if (state) {
            audiences.sender(commandContext.getSender())
                    .sendMessage(messagesConfig.getSpyCommandSpyIsNowEnabled());
        } else {
            audiences.sender(commandContext.getSender())
                    .sendMessage(messagesConfig.getSpyCommandSpyIsNowDisabled());
        }
    }

}

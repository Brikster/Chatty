package ru.brikster.chatty.spy;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpyCommandHandler implements CommandExecutionHandler<CommandSender> {

    @Inject private PlayerDataRepository repository;
    @Inject private MessagesConfig messagesConfig;
    @Inject private BukkitAudiences audiences;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        Player sender = (Player) commandContext.getSender();

        if (repository.isPlayerSpyReceive(sender.getUniqueId())) {
            repository.setPlayerSpyReceive(sender.getUniqueId(), false);
            audiences.sender(sender).sendMessage(messagesConfig.getSpyDisabled());
        } else {
            repository.setPlayerSpyReceive(sender.getUniqueId(), true);
            audiences.sender(sender).sendMessage(messagesConfig.getSpyEnabled());
        }
    }

}

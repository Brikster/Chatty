package ru.brikster.chatty.pm.ignore;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.pm.PmMessageService;
import ru.brikster.chatty.pm.targets.PmMessageTarget;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public final class RemoveIgnoreCommandHandler implements CommandExecutionHandler<CommandSender> {

    @Inject private PmMessageService pmMessageService;
    @Inject private BukkitAudiences audiences;
    @Inject private MessagesConfig messagesConfig;
    @Inject private PlayerDataRepository repository;

    @Override
    public void execute(@NotNull CommandContext<CommandSender> commandContext) {
        Player sender = (Player) commandContext.getSender();

        String targetName = commandContext.get("target");

        UUID targetUuid;

        PmMessageTarget target = pmMessageService.resolveTarget(targetName, false);
        if (target == null) {
            targetUuid = repository.getCachedUuid(targetName);
        } else {
            targetUuid = target.getUuid();
        }

        if (targetUuid == null) {
            audiences.sender(sender).sendMessage(messagesConfig.getPmPlayerNotFound());
            return;
        }

        if (repository.isIgnoredPlayer(sender.getUniqueId(), targetUuid)) {
            repository.createOrUpdateUser(sender.getUniqueId(), sender.getName());
            if (target != null) {
                repository.createOrUpdateUser(target.getUuid(), target.getName());
            }

            repository.removeIgnoredPlayer(sender.getUniqueId(), targetUuid);
            audiences.sender(sender).sendMessage(messagesConfig.getPmYouDontNowIgnore());
        } else {
            audiences.sender(sender).sendMessage(messagesConfig.getPmYouDontIgnore());
        }
    }

}

package ru.brikster.chatty.pm.ignore;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class IgnoreListCommandHandler implements CommandExecutionHandler<CommandSender> {

    @Inject private BukkitAudiences audiences;
    @Inject private MessagesConfig messagesConfig;
    @Inject private PlayerDataRepository repository;

    public IgnoreListCommandHandler() {}

    @Override
    public void execute(@NotNull CommandContext<CommandSender> commandContext) {
        Player sender = (Player) commandContext.getSender();

        String ignoredPlayersList = String.join(", ", repository.getIgnoredPlayersByUsernames(sender));
        if (ignoredPlayersList.isEmpty()) {
            ignoredPlayersList = "(0)";
        }

        audiences.sender(sender).sendMessage(messagesConfig.getPmIgnoreList()
                .replaceText(AdventureUtil.createReplacement("{players}", ignoredPlayersList)));
    }

}

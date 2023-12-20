package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.PmConfig;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class PrivateMessageCommandHandler {

    @Inject private Plugin plugin;
    @Inject private PmConfig pmConfig;
    @Inject private PmMessageService pmMessageService;
    @Inject private MessagesConfig messagesConfig;
    @Inject private BukkitAudiences audiences;
    @Inject private PlayerDataRepository playerDataRepository;

    public void handleCommand(@NotNull CommandContext<@NotNull CommandSender> commandContext,
                              @NotNull CommandSender sender,
                              @NotNull CommandSender target) {
        if (sender == target) {
            audiences.sender(sender)
                    .sendMessage(messagesConfig.getPmCannotPmYourself());
            return;
        }

        String message = commandContext.get("message");

        Component fromComponentFormat = pmMessageService.transformFormat(
                pmConfig.getFromFormat(),
                sender, target, message);

        Component toComponentFormat = pmMessageService.transformFormat(
                pmConfig.getToFormat(),
                sender, target, message);

        audiences.sender(sender).sendMessage(fromComponentFormat);
        pmMessageService.addConversation(sender, target);

        boolean ignored = sender instanceof Player && target instanceof Player
                && playerDataRepository.isIgnoredPlayer((Player) target, ((Player) sender).getUniqueId());

        if (!ignored) {
            var targetAudience = audiences.sender(target);
            targetAudience.sendMessage(toComponentFormat);
            if (pmConfig.isPlaySound()) {
                targetAudience.playSound(pmConfig.getSound());
            }
            pmMessageService.addConversation(target, sender);
        }

        if (pmConfig.getSpy().isEnable()) {
            Component spyComponentFormat = pmMessageService.transformFormat(
                    pmConfig.getSpy().getFormat(),
                    sender, target, message);
            audiences.filter(spyCandidate -> spyCandidate.hasPermission("chatty.spy.pm")
                            && spyCandidate != sender 
                            && (spyCandidate != target || ignored))
                    .sendMessage(spyComponentFormat);
        }

        boolean consoleIsInConversation = sender instanceof ConsoleCommandSender || target instanceof ConsoleCommandSender;

        if (!consoleIsInConversation) {
            plugin.getLogger().info("[PM] " + sender.getName() + " -> " + target.getName() + ": " + message);
        }
    }

}

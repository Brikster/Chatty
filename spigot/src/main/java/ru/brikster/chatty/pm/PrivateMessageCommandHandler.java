package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.pm.targets.PmMessageTarget;
import ru.brikster.chatty.proxy.ProxyService;
import ru.brikster.chatty.repository.player.Mute;
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.MuteFormatter;

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
    @Inject private ProxyService proxyService;

    public void handleCommand(@NotNull CommandContext<@NotNull CommandSender> commandContext,
                              @NotNull CommandSender sender,
                              @NotNull PmMessageTarget target) {
        if (target.isOnline() && sender == target.asCommandSender()) {
            audiences.sender(sender)
                    .sendMessage(messagesConfig.getPmCannotPmYourself());
            return;
        }

        if (sender instanceof Player && !sender.hasPermission("chatty.bypass.mute")) {
            Mute mute = playerDataRepository.getMute(((Player) sender).getUniqueId());
            if (mute != null && !mute.isExpired(System.currentTimeMillis())) {
                audiences.sender(sender).sendMessage(messagesConfig.getMuted()
                        .replaceText(AdventureUtil.createReplacement("{duration}",
                                MuteFormatter.describe(mute, messagesConfig)))
                        .replaceText(AdventureUtil.createReplacement("{reason}",
                                mute.getReason() == null ? "" : mute.getReason())));
                return;
            }
        }

        String message = commandContext.get("message");

        Component fromComponentFormat = pmMessageService.transformFormat(
                pmConfig.getFromFormat(),
                sender, target, message);

        Component toComponentFormat = pmMessageService.transformFormat(
                pmConfig.getToFormat(),
                sender, target, message);

        audiences.sender(sender).sendMessage(fromComponentFormat);
        pmMessageService.addConversation(sender.getName(),
                target.isConsole() ? "Console" : target.getName());

        boolean ignored = sender instanceof Player && !target.isConsole() && target.getUuid() != null
                && playerDataRepository.isIgnoredPlayer(target.getUuid(), ((Player) sender).getUniqueId());

        Component spyComponentFormat = null;

        if (pmConfig.getSpy().isEnable()) {
            spyComponentFormat = pmMessageService.transformFormat(
                    pmConfig.getSpy().getFormat(),
                    sender, target, message);
            audiences.filter(spyCandidate -> spyCandidate.hasPermission("chatty.spy.pm")
                            && !(spyCandidate instanceof ConsoleCommandSender)
                            && playerDataRepository.isEnableSpy(((Player) spyCandidate).getUniqueId())
                            && spyCandidate != sender
                            && (!target.isOnline() || spyCandidate != target.asCommandSender()))
                    .sendMessage(spyComponentFormat);
        }

        String logMessage = "[PM] " + sender.getName() + " -> " + target.getName() + ": " + message;

        if (!ignored) {
            if (target.isOnline()) {
                var targetAudience = audiences.sender(target.asCommandSender());
                targetAudience.sendMessage(toComponentFormat);
                if (pmConfig.isPlaySound()) {
                    targetAudience.playSound(pmConfig.getSound());
                }
            } else {
                proxyService.sendPrivateMessage(target.getName(), toComponentFormat,
                        spyComponentFormat,
                        logMessage,
                        pmConfig.isPlaySound() ? pmConfig.getSound() : null);
            }
            pmMessageService.addConversation(target.isConsole() ? "Console" : target.getName(),
                    sender.getName());
        }

        boolean consoleIsInConversation = sender instanceof ConsoleCommandSender || target.isConsole();

        if (!consoleIsInConversation) {
            plugin.getLogger().info(logMessage);
        }
    }

}

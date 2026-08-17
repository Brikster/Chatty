package ru.brikster.chatty.command.handler;

import ru.brikster.chatty.util.ChattyMessages;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.api.event.ChattyMuteEvent;
import ru.brikster.chatty.api.event.ChattyUnmuteEvent;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.Mute;
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.EventUtil;
import ru.brikster.chatty.util.MuteFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public final class MuteCommandHandler implements CommandExecutionHandler<CommandSender> {

    private final PlayerDataRepository repository;
    private final MessagesConfig messages;
    private final BukkitAudiences audiences;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        CommandSender sender = commandContext.getSender();
        String targetName = commandContext.get("player");
        boolean unmute = Boolean.TRUE.equals(commandContext.getOrDefault("unmute", Boolean.FALSE));

        UUID targetUuid = resolve(targetName);
        if (targetUuid == null) {
            ChattyMessages.send(audiences.sender(sender),
                    messages.getPmPlayerNotFound());
            return;
        }

        if (unmute) {
            if (repository.getMute(targetUuid) == null) {
                ChattyMessages.send(audiences.sender(sender),
                        withPlayer(messages.getMuteCommandNotMuted(), targetName));
                return;
            }
            ChattyUnmuteEvent unmuteEvent = new ChattyUnmuteEvent(sender, targetUuid, targetName);
            EventUtil.callAsynchronously(unmuteEvent);
            if (unmuteEvent.isCancelled()) {
                return;
            }

            repository.clearMute(targetUuid);
            ChattyMessages.send(audiences.sender(sender),
                    withPlayer(messages.getMuteCommandUnmuted(), targetName));
            return;
        }

        String options = commandContext.<String>getOptional("options").orElse("").trim();
        String durationToken = options.isEmpty() ? null : options.split("\\s+", 2)[0];
        Long millis = durationToken == null ? null : MuteFormatter.parseDuration(durationToken);

        String reason;
        if (millis == null) {
            reason = options.isEmpty() ? null : options;
        } else {
            String[] split = options.split("\\s+", 2);
            reason = split.length > 1 ? split[1] : null;
        }

        long until = millis == null ? Mute.PERMANENT : System.currentTimeMillis() + millis;

        ChattyMuteEvent muteEvent = new ChattyMuteEvent(sender, targetUuid, targetName, until, reason);
        EventUtil.callAsynchronously(muteEvent);
        if (muteEvent.isCancelled()) {
            return;
        }

        Mute mute = new Mute(muteEvent.getUntil(), muteEvent.getReason());

        repository.createOrUpdateUser(targetUuid, targetName);
        repository.setMute(targetUuid, mute);

        ChattyMessages.send(audiences.sender(sender),
                withPlayer(messages.getMuteCommandSuccess(), targetName)
                .replaceText(AdventureUtil.createReplacement("{duration}", MuteFormatter.describe(mute, messages))));
    }

    private UUID resolve(String name) {
        UUID cached = repository.getCachedUuid(name);
        if (cached != null) {
            return cached;
        }
        OfflinePlayer offline = Bukkit.getPlayerExact(name);
        return offline == null ? null : offline.getUniqueId();
    }

    private static Component withPlayer(Component component, String name) {
        return component.replaceText(AdventureUtil.createReplacement("{player}", name));
    }

}

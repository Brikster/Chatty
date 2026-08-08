package ru.brikster.chatty.command.handler;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.Mute;
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;
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
            audiences.sender(sender).sendMessage(messages.getPmPlayerNotFound());
            return;
        }

        if (unmute) {
            if (repository.getMute(targetUuid) == null) {
                audiences.sender(sender).sendMessage(withPlayer(messages.getMuteCommandNotMuted(), targetName));
                return;
            }
            repository.clearMute(targetUuid);
            audiences.sender(sender).sendMessage(withPlayer(messages.getMuteCommandUnmuted(), targetName));
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

        Mute mute = millis == null
                ? Mute.permanent(reason)
                : new Mute(System.currentTimeMillis() + millis, reason);

        repository.createOrUpdateUser(targetUuid, targetName);
        repository.setMute(targetUuid, mute);

        audiences.sender(sender).sendMessage(withPlayer(messages.getMuteCommandSuccess(), targetName)
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

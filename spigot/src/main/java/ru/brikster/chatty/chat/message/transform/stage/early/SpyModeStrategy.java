package ru.brikster.chatty.chat.message.transform.stage.early;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.context.MessageContextKeys;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

@Singleton
public final class SpyModeStrategy implements MessageTransformStrategy<String> {

    private static final long WARN_INTERVAL_MILLIS = 60_000L;

    @Inject private PlayerDataRepository repository;
    @Inject private Plugin plugin;

    private long lastWarnAt;

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        List<Player> recipients = new ArrayList<>(context.getRecipients());

        MessageTransformResultBuilder<String> builder = MessageTransformResultBuilder.fromContext(context);

        List<Player> spies = new ArrayList<>();
        if (context.getChat().isEnableSpy()) {
            try {
                Set<UUID> spyEnabled = null;
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onlinePlayer.hasPermission("chatty.spy." + context.getChat().getId())
                            || recipients.contains(onlinePlayer)) {
                        continue;
                    }
                    if (spyEnabled == null) {
                        spyEnabled = repository.getSpyEnabledUuids();
                    }
                    if (spyEnabled.contains(onlinePlayer.getUniqueId())) {
                        recipients.add(onlinePlayer);
                        spies.add(onlinePlayer);
                    }
                }
            } catch (RuntimeException e) {
                recipients.removeAll(spies);
                spies.clear();
                warn("Cannot resolve chat spies, delivering without them", e);
            }

            builder.withMetadata(MessageContextKeys.SPY_RECIPIENTS, spies);
        }

        try {
            Set<UUID> whoIgnoreUuids = repository.getWhoIgnoreUuids(context.getSender());
            recipients.removeIf(recipient -> whoIgnoreUuids.contains(recipient.getUniqueId()));
        } catch (RuntimeException e) {
            warn("Cannot read ignore lists, delivering to everyone", e);
        }

        try {
            repository.createOrUpdateUser(context.getSender().getUniqueId(), context.getSender().getName());
        } catch (RuntimeException e) {
            warn("Cannot store the player record", e);
        }

        return builder
                .withRecipients(recipients)
                .build();
    }

    private void warn(String message, Throwable cause) {
        long now = System.currentTimeMillis();
        if (now - lastWarnAt < WARN_INTERVAL_MILLIS) {
            return;
        }
        lastWarnAt = now;
        plugin.getLogger().log(Level.WARNING, message, cause);
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

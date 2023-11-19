package ru.brikster.chatty.chat.message.transform.stage.early;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.api.chat.range.Ranges;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public final class RecipientsWithRangeStrategy implements MessageTransformStrategy<String> {

    @Inject private PlayerDataRepository repository;

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        List<Player> recipients = Bukkit.getOnlinePlayers()
                .stream()
                .filter(recipient -> Ranges.isApplicable(context.getSender(), recipient, context.getChat().getRange()))
                .collect(Collectors.toList());

        MessageTransformResultBuilder<String> builder = MessageTransformResultBuilder.<String>fromContext(context)
                .withRecipients(recipients);

        List<Player> spies = new ArrayList<>();
        if (context.getChat().isEnableSpy()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("chatty.spy." + context.getChat().getName())
                    && !recipients.contains(onlinePlayer)) {
                    recipients.add(onlinePlayer);
                    spies.add(onlinePlayer);
                }
            }

            builder.withMetadata("spy-recipients", spies);
        }

        Set<UUID> whoIgnoreUuids = repository.getWhoIgnoreUuids(context.getSender());
        recipients.removeIf(recipient -> whoIgnoreUuids.contains(recipient.getUniqueId()));

        repository.createOrUpdateUser(context.getSender().getUniqueId(), context.getSender().getName());

        return builder.build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

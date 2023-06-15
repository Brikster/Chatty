package ru.brikster.chatty.chat.message.transform.stage.early.range;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.api.chat.message.strategy.stage.EarlyMessageTransformStrategy;
import ru.brikster.chatty.api.chat.range.Ranges;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;

import java.util.Set;
import java.util.stream.Collectors;

public final class RangeLimiterMessageTransformStrategy implements EarlyMessageTransformStrategy {

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        Set<Player> recipients = Bukkit.getOnlinePlayers()
                .stream()
                .filter(recipient -> Ranges.isApplicable(context.getSender(), recipient, context.getChat().getRange()))
                .collect(Collectors.toSet());
        return MessageTransformResultBuilder.<String>fromContext(context)
                .withRecipients(recipients)
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

package ru.brikster.chatty.chat.message.strategy.impl.range;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.range.Ranges;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;

import java.util.HashSet;
import java.util.Set;

public final class RangeLimiterMessageTransformStrategy implements MessageTransformStrategy<String, String> {

    private static final MessageTransformStrategy<String, String> INSTANCE = new RangeLimiterMessageTransformStrategy();

    @Override
    public @NotNull Result<String> handle(MessageContext<String> context) {
        Set<Player> recipients = new HashSet<>(context.getRecipients());
        Set<Player> removedRecipients = new HashSet<>(context.getRecipients());
        Set<Player> addedRecipients = new HashSet<>();

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(recipient -> Ranges.isApplicable(context.getSender(), recipient, context.getChat().getRange()))
                .forEach(recipient -> {
                    if (recipients.contains(recipient)) {
                        removedRecipients.remove(recipient);
                    } else {
                        addedRecipients.add(recipient);
                    }
                });

        recipients.removeAll(removedRecipients);
        recipients.addAll(addedRecipients);

        MessageContext<String> newContext = new MessageContextImpl<>(context);
        newContext.setRecipients(recipients);

        return ResultImpl.<String>builder()
                .newContext(newContext)
                .addedRecipients(addedRecipients)
                .removedRecipients(removedRecipients)
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    public static MessageTransformStrategy<String, String> instance() {
        return INSTANCE;
    }

}

package ru.brikster.chatty.chat.message.transform.stage.early.economy;

import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.chat.message.transform.stage.EarlyMessageTransformStrategy;

@RequiredArgsConstructor
public final class VaultEconomyMessageTransformStrategy implements EarlyMessageTransformStrategy {

    private final @NotNull Economy vaultEconomy;
    private final int cost;

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        if (!vaultEconomy.has(context.getSender(), cost)) {
            // TODO send message no sufficient money
            return MessageTransformResultBuilder.<String>fromContext(context)
                    .withCancelled()
                    .build();
        } else {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

}

package ru.brikster.chatty.chat.handle.strategy.impl.vault;

import net.milkbowl.vault.economy.Economy;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageHandleStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VaultEconomyMessageHandleStrategy implements MessageHandleStrategy<String, String> {

    private final @NotNull Economy vaultEconomy;
    private final int cost;

//    public VaultEconomyMessageHandleStrategy(Economy vaultEconomy, ) {
//        RegisteredServiceProvider<Economy> economyRegisteredServiceProvider
//                = Bukkit.getServicesManager().getRegistration(Economy.class);
//
//        if (economyRegisteredServiceProvider != null) {
//            this.vaultEconomy = economyRegisteredServiceProvider.getProvider();
//        } else {
//            this.vaultEconomy = null;
//        }
//    }

    @Override
    public Result<String> handle(MessageContext<String> context) {
        if (!vaultEconomy.has(context.getSender(), cost)) {
            // TODO send message no sufficient money

            MessageContext<String> newContext = new MessageContextImpl<>(context);
            newContext.setCancelled(true);

            return ResultImpl.<String>builder()
                    .newContext(newContext)
                    .becameCancelled(!context.isCancelled())
                    .build();
        } else {
            return ResultImpl.<String>builder()
                    .newContext(new MessageContextImpl<>(context))
                    .build();
        }
    }

}

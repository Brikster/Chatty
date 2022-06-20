package ru.brikster.chatty.chat.handle.strategy.impl.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageHandleStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;

public class PlaceholderApiMessageHandleStrategy implements MessageHandleStrategy<String, String> {

    @Override
    public Result<String> handle(MessageContext<String> context) {
        String updatedMessage = PlaceholderAPI.setPlaceholders(context.getSender(), context.getMessage());

        MessageContext<String> newContext = new MessageContextImpl<>(context);
        newContext.setMessage(updatedMessage);

        return ResultImpl.<String>builder()
                .newContext(newContext)
                .messageUpdated(updatedMessage.equals(context.getMessage()))
                .build();
    }

}

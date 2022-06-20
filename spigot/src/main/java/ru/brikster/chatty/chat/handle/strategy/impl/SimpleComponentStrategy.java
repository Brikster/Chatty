package ru.brikster.chatty.chat.handle.strategy.impl;

import net.kyori.adventure.text.Component;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageHandleStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.convert.component.MiniMessageConverter;

import javax.inject.Inject;

public class SimpleComponentStrategy implements MessageHandleStrategy<String, Component> {

    @Inject
    private ComponentConverter converter = new MiniMessageConverter();

    @Override
    public Result<Component> handle(MessageContext<String> context) {
        Component message = converter.convert(context.getMessage());

        MessageContext<Component> newContext = new MessageContextImpl<Component>(
                context.isCancelled(),
                context.getFormat(),
                context.getRecipients(),
                message,
                context.getChat(),
                context.getSender()
        );

        return ResultImpl.<Component>builder()
                .newContext(newContext)
                .messageUpdated(true)
                .build();
    }

}

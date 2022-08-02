package ru.brikster.chatty.chat.message.strategy.impl;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.convert.component.MiniMessageConverter;

import javax.inject.Inject;

public class ConvertComponentMessageTransformStrategy implements MessageTransformStrategy<String, Component> {

    private static final ConvertComponentMessageTransformStrategy INSTANCE = new ConvertComponentMessageTransformStrategy();

    private ConvertComponentMessageTransformStrategy() {}

    @Inject
    private ComponentConverter converter = new MiniMessageConverter();

    @Override
    public @NotNull Result<Component> handle(MessageContext<String> context) {
        Component message = converter.convert(context.getMessage());

        MessageContext<Component> newContext = new MessageContextImpl<>(
                context.getChat(),
                context.getSender(),
                context.isCancelled(),
                context.getFormat(),
                context.getRecipients(),
                message
        );

        return ResultImpl.<Component>builder()
                .newContext(newContext)
                .messageUpdated(true)
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.LATE;
    }

    public static ConvertComponentMessageTransformStrategy instance() {
        return INSTANCE;
    }

}

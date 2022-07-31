package ru.brikster.chatty.chat.handle.strategy.impl.moderation;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;

import javax.inject.Inject;

public class CapsModerationMessageTransformStrategy implements MessageTransformStrategy<String, String> {

    private final int percent;
    private final int length;
    private final boolean useBlock;
    @Inject
    private Configuration config;

    public CapsModerationMessageTransformStrategy() {
        ConfigurationNode node = config.getNode("moderation.caps");
        this.useBlock = node.getNode("block").getAsBoolean(true);
        this.percent = node.getNode("percent").getAsInt(80);
        this.length = node.getNode("length").getAsInt(6);
    }

    @Override
    public @NotNull Result<String> handle(MessageContext<String> context) {
        String message = context.getMessage();

        if (message.length() >= length
                && calculatePercent(message) >= percent) {
            message = message.toLowerCase();

            MessageContext<String> newContext = new MessageContextImpl<>(context);
            newContext.setMessage(message);

            if (useBlock) {
                newContext.setCancelled(true);
                return ResultImpl.<String>builder()
                        .newContext(newContext)
                        .messageUpdated(true)
                        .becameCancelled(!context.isCancelled())
                        .build();
            } else {
                return ResultImpl.<String>builder()
                        .newContext(newContext)
                        .messageUpdated(true)
                        .build();
            }
        }

        return ResultImpl.<String>builder()
                .newContext(new MessageContextImpl<>(context))
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    private int calculatePercent(String message) {
        int codePoint;
        int length = 0;
        int capsLength = 0;
        for (char c : message.toCharArray()) {
            codePoint = c;
            if (Character.isLetter(codePoint)) {
                length++;
                if (codePoint == Character.toUpperCase(codePoint)
                        && (Character.toLowerCase(codePoint) != Character.toUpperCase(codePoint))) {
                    capsLength++;
                }
            }
        }

        return (int) ((double) capsLength / (double) length * 100);
    }

}

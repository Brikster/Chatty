package ru.brikster.chatty.chat.message.strategy.impl.moderation;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.context.MessageContext.Tag;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.ModerationConfig;
import ru.brikster.chatty.config.type.ModerationConfig.CapsModerationConfig;

import javax.inject.Inject;

public final class CapsModerationMessageTransformStrategy implements MessageTransformStrategy<String, String> {

    private static final CapsModerationMessageTransformStrategy INSTANCE = new CapsModerationMessageTransformStrategy();

    private final static Tag<Boolean> CAPS_MODERATION_BLOCK_TAG = Tag.Create("caps-moderation-block", Boolean.class);

    private final int percent;
    private final int length;
    private final boolean useBlock;

    @Inject private BukkitAudiences audiences;

    @Inject private MessagesConfig messages;
    @Inject private ModerationConfig moderationConfig;

    private CapsModerationMessageTransformStrategy() {
        CapsModerationConfig config = moderationConfig.getCaps();
        this.useBlock = config.isBlock();
        this.percent = config.getPercent();
        this.length = config.getLength();
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
                        .newContext(newContext.withTag(CAPS_MODERATION_BLOCK_TAG, true))
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

    @Override
    public void handleFinally(MessageContext<String> context) {
        if (context.isCancelled() && context.getTag(CAPS_MODERATION_BLOCK_TAG).orElse(false)) {
            audiences.player(context.getSender()).sendMessage(messages.getCapsFound());
        }
    }

    public static CapsModerationMessageTransformStrategy instance() {
        return INSTANCE;
    }

}

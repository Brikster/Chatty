package ru.brikster.chatty.chat.message.transform.stage.early.moderation;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.ModerationConfig;
import ru.brikster.chatty.config.type.ModerationConfig.SwearModerationConfig;
import ru.brikster.chatty.repository.swear.SwearRepository;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SwearModerationMessageTransformStrategy implements MatcherMessageTransformStrategy {

    private final String replacement;
    private final boolean useBlock;

    private final Pattern swearPattern;

    @Inject private BukkitAudiences audiences;
    @Inject private MessagesConfig messages;
    @Inject private ModerationConfig moderationConfig;
    @Inject private SwearRepository swearRepository;

    private SwearModerationMessageTransformStrategy() {
        SwearModerationConfig config = moderationConfig.getSwear();
        this.replacement = config.getReplacement();
        this.useBlock = config.isBlock();

        if (swearRepository.getSwears().size() != 0) {
            this.swearPattern =
                    Pattern.compile(String.join("|", swearRepository.getSwears()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } else {
            this.swearPattern = null;
        }
    }

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        String message = context.getMessage();
        String matchedMessage = match(message, swearPattern);

        boolean hasViolations = !message.equals(matchedMessage);
        MessageTransformResult<String> messageTransformResult = getMatcherResult(context, matchedMessage, !hasViolations, useBlock);

        if (hasViolations) {
            audiences.player(context.getSender()).sendMessage(messages.getSwearFound());
        }

        return messageTransformResult;
    }

    private @NotNull String match(@NotNull String message, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (!swearRepository.getWhitelist().contains(matcher.group().trim())) {
                matcher.appendReplacement(buffer, replacement);
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

}

package ru.brikster.chatty.chat.message.transform.stage.early.moderation;

import com.google.inject.Singleton;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.ModerationConfig;
import ru.brikster.chatty.config.type.ModerationConfig.SwearModerationConfig;
import ru.brikster.chatty.repository.swear.SwearRepository;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public final class SwearModerationStrategyModeration implements ModerationMatcherStrategy {

    private final BukkitAudiences audiences;
    private final MessagesConfig messages;
    private final SwearRepository swearRepository;

    private final String replacement;
    private final boolean useBlock;

    private final Pattern swearPattern;

    @Inject
    public SwearModerationStrategyModeration(BukkitAudiences audiences, MessagesConfig messages, ModerationConfig moderationConfig, SwearRepository swearRepository) {
        this.audiences = audiences;
        this.messages = messages;
        this.swearRepository = swearRepository;

        SwearModerationConfig config = moderationConfig.getSwear();
        this.replacement = config.getReplacement();
        this.useBlock = config.isBlock();

        if (!swearRepository.getSwears().isEmpty()) {
            this.swearPattern =
                    Pattern.compile(swearRepository.getSwears()
                                    .stream()
                                    .map(swear -> "(".concat(swear).concat(")"))
                                    .collect(Collectors.joining("|")),
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

    private @NotNull String match(@NotNull String message, @Nullable Pattern pattern) {
        if (pattern == null) {
            return message;
        }

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

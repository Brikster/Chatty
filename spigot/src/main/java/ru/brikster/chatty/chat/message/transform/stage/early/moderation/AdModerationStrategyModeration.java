package ru.brikster.chatty.chat.message.transform.stage.early.moderation;

import com.google.inject.Singleton;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.ModerationConfig;
import ru.brikster.chatty.config.file.ModerationConfig.AdvertisementModerationConfig;

import javax.inject.Inject;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public final class AdModerationStrategyModeration implements ModerationMatcherStrategy {

    private final BukkitAudiences audiences;
    private final MessagesConfig messages;

    private final Set<String> whitelist;
    private final boolean useBlock;
    private final String replacement;

    private final Pattern ipPattern;
    private final Pattern webPattern;

    @Inject
    public AdModerationStrategyModeration(BukkitAudiences audiences, MessagesConfig messages, ModerationConfig moderationConfig) {
        this.audiences = audiences;
        this.messages = messages;

        AdvertisementModerationConfig config = moderationConfig.getAdvertisement();

        this.whitelist = config.getWhitelist();
        this.useBlock = config.isBlock();
        this.replacement = config.getReplacement();

        this.ipPattern = config.getIpPattern();
        this.webPattern = config.getLinkPattern();
    }

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        String message = context.getMessage();
        String matchedMessage = match(message, ipPattern);
        matchedMessage = match(matchedMessage, webPattern);

        boolean hasViolations = !message.equals(matchedMessage);
        MessageTransformResult<String> messageTransformResult = getMatcherResult(context, matchedMessage, !hasViolations, useBlock);

        if (hasViolations) {
            audiences.player(context.getSender()).sendMessage(messages.getAdvertisementFound());
        }

        return messageTransformResult;
    }

    private @NotNull String match(@NotNull String message, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            if (!this.whitelist.contains(matcher.group().trim())) {
                matcher.appendReplacement(builder, replacement);
            }
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

}

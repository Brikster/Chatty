package ru.brikster.chatty.chat.message.strategy.impl.moderation;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.ModerationConfig;
import ru.brikster.chatty.config.type.ModerationConfig.AdvertisementModerationConfig;

import javax.inject.Inject;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdModerationMessageTransformStrategy implements MatcherMessageTransformStrategy {

    private static final AdModerationMessageTransformStrategy INSTANCE = new AdModerationMessageTransformStrategy();

    private final Set<String> whitelist;
    private final boolean useBlock;
    private final String replacement;

    private final Pattern ipPattern;
    private final Pattern webPattern;

    @Inject private BukkitAudiences audiences;
    @Inject private MessagesConfig messages;
    @Inject private ModerationConfig moderationConfig;

    private AdModerationMessageTransformStrategy() {
        AdvertisementModerationConfig config = moderationConfig.getAdvertisement();

        this.whitelist = config.getWhitelist();
        this.useBlock = config.isBlock();
        this.replacement = config.getReplacement();

        this.ipPattern = config.getIpPattern();
        this.webPattern = config.getLinkPattern();
    }

    @Override
    public @NotNull Result<String> handle(MessageContext<String> context) {
        String message = context.getMessage();
        String matchedMessage = match(message, ipPattern);
        matchedMessage = match(matchedMessage, webPattern);

        boolean hasViolations = !message.equals(matchedMessage);
        Result<String> result = getMatcherResult(context, matchedMessage, !hasViolations, useBlock);

        if (hasViolations) {
            audiences.player(context.getSender()).sendMessage(messages.getAdvertisementFound());
        }

        return result;
    }

    private @NotNull String match(@NotNull String message, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (!this.whitelist.contains(matcher.group().trim())) {
                matcher.appendReplacement(buffer, replacement);
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public static AdModerationMessageTransformStrategy instance() {
        return INSTANCE;
    }

}

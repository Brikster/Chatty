package ru.brikster.chatty.chat.handle.strategy.impl.moderation;

import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.repository.swear.SwearRepository;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwearModerationMessageHandleStrategy implements MatcherMessageHandleStrategy {

    private final String replacement;
    private final boolean useBlock;

    private final Pattern swearPattern;
    @Inject
    private Configuration config;

    @Inject
    private SwearRepository swearRepository;

    public SwearModerationMessageHandleStrategy() {
        ConfigurationNode node = config.getNode("moderation.swear");
        this.replacement = node.getNode("replacement").getAsString("<swear>");
        this.useBlock = node.getNode("block").getAsBoolean(true);

        if (swearRepository.getSwears().size() != 0) {
            this.swearPattern =
                    Pattern.compile(String.join("|", swearRepository.getSwears()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } else {
            this.swearPattern = null;
        }
    }

    @Override
    public Result<String> handle(MessageContext<String> context) {
        String message = context.getMessage();
        String matchedMessage = match(message, swearPattern);

        return getMatcherResult(context, matchedMessage, message.equals(matchedMessage), useBlock);
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

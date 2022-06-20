package ru.brikster.chatty.chat.handle.strategy.impl.moderation;

import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdModerationMessageHandleStrategy implements MatcherMessageHandleStrategy {

    private static final String IP = "\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,)(-]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,7})?";
    private static final String WEB = "(?i)\\b(https?://)?[\\w.а-яА-Я-]+\\.([a-z]{2,4}|[рР][фФ]|[уУ][кК][рР])\\b(:\\d{2,7})?(/\\S+)?";

    @Inject
    private Configuration config;

    private final HashSet<String> whitelist;
    private final boolean useBlock;
    private final String replacement;

    private final Pattern ipPattern;
    private final Pattern webPattern;

    public AdModerationMessageHandleStrategy() {
        ConfigurationNode node = config.getNode("moderation.advertisement");
        this.whitelist = new HashSet<>(node.getNode("whitelist").getAsStringList());
        this.useBlock = node.getNode("block").getAsBoolean(true);
        this.replacement = node.getNode("replacement").getAsString("<ad>");

        this.ipPattern = Pattern.compile(node.getNode("patterns.ip").getAsString(IP));
        this.webPattern = Pattern.compile(node.getNode("patterns.web").getAsString(WEB));
    }

    @Override
    public Result<String> handle(MessageContext<String> context) {
        String message = context.getMessage();
        String matchedMessage = match(message, ipPattern);
        matchedMessage = match(matchedMessage, webPattern);

        return getMatcherResult(context, matchedMessage, message.equals(matchedMessage), useBlock);
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

}

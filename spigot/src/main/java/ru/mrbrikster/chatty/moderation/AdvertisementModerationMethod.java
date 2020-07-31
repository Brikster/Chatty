package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;
import ru.mrbrikster.chatty.util.CachedObject;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvertisementModerationMethod extends ModifyingSubstringsModerationMethod {
    private static final String IP =
            "\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,-)(]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,8})?";
    private static final String WEB =
            "\\b(https?:\\/\\/)?[\\w\\.а-яА-Я-]+\\.([a-z]{2,4}|[рР][фФ]|[уУ][кК][рР])\\b(:\\d{2,5})?(\\/\\S+)?";
    private static final CachedObject<String, Pattern> cachedIp = new CachedObject<>(IP, Pattern.compile(IP));
    private static final CachedObject<String, Pattern> cachedWeb = new CachedObject<>(WEB, Pattern.compile(WEB));

    private String editedMessage;
    private boolean checked = false, result = false;

    private final Set<String> whitelist;

    private final Pattern ipPattern;
    private final Pattern webPattern;

    @Getter private final String replacement;
    @Getter private final boolean useBlock;

    AdvertisementModerationMethod(ConfigurationNode configurationNode, String message, String lastFormatColors) {
        super(message, lastFormatColors);

        this.whitelist = new HashSet<>(configurationNode.getNode("whitelist").getAsStringList());
        this.ipPattern = Pattern.compile(configurationNode.getNode("patterns.ip")
                .getAsString("\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,-)(]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,8})?"));
        this.webPattern = Pattern.compile(configurationNode.getNode("patterns.web")
                .getAsString("\\b(https?:\\/\\/)?[\\w\\.а-яА-Я-]+\\.([a-z]{2,4}|[рР][фФ]|[уУ][кК][рР])\\b(:\\d{2,5})?(\\/\\S+)?"));
        this.replacement = TextUtil.stylish(configurationNode.getNode("replacement").getAsString("<ads>"));
        this.useBlock = configurationNode.getNode("block").getAsBoolean(true);
    }

    @Override
    public String getEditedMessage() {
        if (this.editedMessage == null) {
            this.isBlocked();
        }

        return this.editedMessage;
    }

    @Override
    public boolean isBlocked() {
        if (this.checked) {
            return this.result;
        }

        if (this.editedMessage == null) {
            this.editedMessage = this.message;
        }

        this.result = match(ipPattern) | match(webPattern) || this.result;

        this.checked = true;

        return this.result;
    }

    @Override
    public String getLogPrefix() {
        return "ADVERTISEMENT";
    }

    @Override
    public String getWarningMessageKey() {
        return "advertisement-found";
    }

    private boolean match(Pattern pattern) {
        Matcher matcher = pattern.matcher(this.editedMessage);

        int prevIndex = 0;
        StringBuilder builder = new StringBuilder();

        boolean containsAds = false;
        while (matcher.find()) {
            String group = matcher.group();

            builder.append(this.editedMessage, prevIndex, matcher.start());
            prevIndex = matcher.end();

            if (this.whitelist.contains(group.trim())) {
                builder.append(this.editedMessage, matcher.start(), matcher.end());
            } else {
                containsAds = true;

                String lastColors = TextUtil.getLastColors(message.substring(0, matcher.start()));
                if (lastColors.isEmpty()) lastColors = lastFormatColors;

                builder.append(this.replacement).append(lastColors);
            }
        }

        if (prevIndex < this.editedMessage.length()) {
            builder.append(this.editedMessage, prevIndex, this.editedMessage.length());
        }

        this.editedMessage = builder.toString();
        return containsAds;
    }

}

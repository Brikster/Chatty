package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.chatty.config.ConfigurationNode;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdvertisementModerationMethod extends ModerationMethod {

    private String editedMessage;
    private boolean alreadyChecked = false, previousResult = false;

    private final List<String> whitelist;
    private Pattern ipPattern, webPattern;
    private String adPlaceholder;
    @Getter
    private boolean useBlock;

    AdvertisementModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);
        this.editedMessage = null;

        this.whitelist = configurationNode.getNode("whitelist")
                .getAsStringList().stream().map(String::toLowerCase).collect(Collectors.toList());
        this.ipPattern = Pattern.compile(configurationNode.getNode("patterns.ip")
                .getAsString("(?:\\d{1,3}[.,\\-:;\\/()=?}+ ]{1,4}){3}\\d{1,3}"));
        this.webPattern = Pattern.compile(configurationNode.getNode("patterns.web")
                .getAsString("[-a-zA-Z0-9@:%_\\+~#?&//=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+~#?&//=]*)?"));
        this.adPlaceholder = configurationNode.getNode("replacement").getAsString("<ads>");
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
        if (this.alreadyChecked) {
            return this.previousResult;
        }
        this.previousResult = match(ipPattern, str -> str);
        this.previousResult = this.previousResult || match(webPattern, str -> str
                .replaceAll("www.", "")
                .replaceAll("http://", "")
                .replaceAll("https://", ""));

        this.alreadyChecked = true;
        return this.previousResult;
    }

    private boolean match(Pattern pattern, Function<String, String> modifyString) {
        Matcher matcher = pattern.matcher(this.message);

        int prevIndex = 0;
        StringBuilder builder = new StringBuilder();

        boolean hasAds = false;
        while (matcher.find()) {
            String group = matcher.group();

            builder.append(this.message, prevIndex, matcher.start());
            prevIndex = matcher.end();

            String ad = group.trim().toLowerCase();
            ad = modifyString.apply(ad);

            if (this.whitelist.contains(ad)) {
                builder.append(this.message, matcher.start(), matcher.end());
            } else {
                hasAds = true;
                builder.append(this.adPlaceholder);
            }
        }

        if (prevIndex < this.message.length()) {
            builder.append(this.message, prevIndex, this.message.length());
        }
        this.editedMessage = builder.toString();
        return hasAds;
    }

}

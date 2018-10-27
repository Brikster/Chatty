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
    private boolean checked = false, result = false;

    private final List<String> whitelist;
    private Pattern ipPattern, webPattern;
    private String replacement;
    @Getter private boolean useBlock;

    AdvertisementModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);

        this.whitelist = configurationNode.getNode("whitelist")
                .getAsStringList().stream().map(String::toLowerCase).collect(Collectors.toList());
        this.ipPattern = Pattern.compile(configurationNode.getNode("patterns.ip")
                .getAsString("(?:\\d{1,3}[.,\\-:;\\/()=?}+ ]{1,4}){3}\\d{1,3}"));
        this.webPattern = Pattern.compile(configurationNode.getNode("patterns.web")
                .getAsString("[-a-zA-Z0-9@:%_\\+~#?&//=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+~#?&//=]*)?"));
        this.replacement = configurationNode.getNode("replacement").getAsString("<ads>");
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

        this.result = match(ipPattern, string -> string);
        this.result = match(webPattern, string -> string
                .replaceAll(Pattern.quote("www."), "")
                .replaceAll(Pattern.quote("http://"), "")
                .replaceAll(Pattern.quote("https://"), "")
        ) || this.result;

        this.checked = true;

        return this.result;
    }

    private boolean match(Pattern pattern, Function<String, String> modifyFunction) {
        Matcher matcher = pattern.matcher(this.editedMessage);

        int prevIndex = 0;
        StringBuilder builder = new StringBuilder();

        boolean containsAds = false;
        while (matcher.find()) {
            String group = matcher.group();

            builder.append(this.editedMessage, prevIndex, matcher.start());
            prevIndex = matcher.end();

            String ad = modifyFunction.apply(group.trim().toLowerCase());

            if (this.whitelist.contains(ad)) {
                builder.append(this.editedMessage, matcher.start(), matcher.end());
            } else {
                containsAds = true;
                builder.append(this.replacement);
            }
        }

        if (prevIndex < this.editedMessage.length()) {
            builder.append(this.editedMessage, prevIndex, this.editedMessage.length());
        }

        this.editedMessage = builder.toString();
        return containsAds;
    }

}

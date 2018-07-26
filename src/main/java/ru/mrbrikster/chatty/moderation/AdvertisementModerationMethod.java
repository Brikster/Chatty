package ru.mrbrikster.chatty.moderation;

import ru.mrbrikster.chatty.config.ConfigurationNode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvertisementModerationMethod extends ModerationMethod {

    private final List<String> whitelist;
    private Pattern ipPattern, webPattern;

    AdvertisementModerationMethod(ConfigurationNode configurationNode, String message) {
        super(configurationNode, message);

        this.whitelist = configurationNode.getNode("whitelist")
                .getAsStringList();
        this.ipPattern = Pattern.compile(configurationNode.getNode("patterns.ip")
                .getAsString("(?:\\d{1,3}[.,\\-:;\\/()=?}+ ]{1,4}){3}\\d{1,3}"));
        this.webPattern = Pattern.compile(configurationNode.getNode("patterns.web")
                .getAsString("[-a-zA-Z0-9@:%_\\+~#?&//=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+~#?&//=]*)?"));
    }

    @Override
    public String getEditedMessage() {
        return message;
    }

    @Override
    public boolean isPassed() {
        return !containsIP(message) && !containsURL(message);
    }

    private boolean containsIP(String message) {
        Matcher regexMatcher = ipPattern.matcher(message);

        while (regexMatcher.find()) {
            if (regexMatcher.group().length() != 0
                    && ipPattern.matcher(message).find()) {
                String advertisement = regexMatcher.group().trim();

                if (!whitelist.contains(advertisement)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsURL(String message) {
        Matcher regexMatcher = webPattern.matcher(message);

        while (regexMatcher.find()) {
            String advertisement = regexMatcher.group().trim()
                    .replaceAll("www.", "")
                    .replaceAll("http://", "")
                    .replaceAll("https://", "");

            if (regexMatcher.group().length() != 0
                    && advertisement.length() != 0
                    && webPattern.matcher(message).find()) {
                if (!whitelist.contains(advertisement)) {
                    return true;
                }
            }
        }

        return false;
    }

}

package ru.mrbrikster.chatty.moderation;

import com.google.common.io.Files;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;
import ru.mrbrikster.chatty.util.TextUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwearModerationMethod extends ModerationMethod {

    private final String replacement;
    private final List<String> words;
    @Getter private final boolean useBlock;

    private static Pattern swearsPattern;
    private static List<Pattern> swearsWhitelist = new ArrayList<>();
    private static File swearsDirectory;
    private static File swearsFile;
    private static File whitelistFile;
    private String editedMessage;

    SwearModerationMethod(ConfigurationNode configurationNode, String message) {
        super(message);

        this.replacement = TextUtil.stylish(configurationNode.getNode("replacement").getAsString("<swear>"));
        this.words = new ArrayList<>();
        this.useBlock = configurationNode.getNode("block").getAsBoolean(true);
    }

    static void init(JavaPlugin javaPlugin) {
        SwearModerationMethod.swearsDirectory = new File(javaPlugin.getDataFolder(), "swears");
        SwearModerationMethod.swearsFile = new File(swearsDirectory, "swears.txt");
        SwearModerationMethod.whitelistFile = new File(swearsDirectory, "whitelist.txt");

        if (!swearsDirectory.exists()) {
            swearsDirectory.mkdir();
        }

        if (!swearsFile.exists()) {
            try {
                swearsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!whitelistFile.exists()) {
            try {
                whitelistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            StringBuilder pattern = new StringBuilder();
            for (String swear : Files.readLines(swearsFile, StandardCharsets.UTF_8)) {
                if (swear.isEmpty())
                    continue;

                if (pattern.length() == 0) {
                    pattern.append(swear);
                } else {
                    pattern.append("|").append(swear);
                }
            }

            SwearModerationMethod.swearsPattern = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
            SwearModerationMethod.swearsWhitelist = Files.readLines(whitelistFile, StandardCharsets.UTF_8).stream().map(whitelistPattern
                    -> Pattern.compile(whitelistPattern.toLowerCase(), Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getWhitelistFile() {
        return whitelistFile;
    }

    public static void addWord(Pattern pattern) {
        swearsWhitelist.add(pattern);
    }

    public List<String> getWords() {
        return words;
    }

    @Override
    public String getEditedMessage() {
        if (editedMessage != null)
            return editedMessage;

        this.editedMessage = message;
        Matcher matcher = swearsPattern.matcher(message.toLowerCase());

        int previousWordStart = -1;
        int previousWordEnd = -1;
        while (matcher.find()) {
            if (matcher.group().trim().isEmpty()) {
                continue;
            }

            int[] wordStartAndEndArray = getWord(message, matcher.start(), matcher.end());

            if (previousWordStart == wordStartAndEndArray[0] && previousWordEnd == wordStartAndEndArray[1]) {
                continue;
            }

            String swear = message.substring(previousWordStart = wordStartAndEndArray[0], previousWordEnd = wordStartAndEndArray[1]);

            boolean whitelisted = false;
            for (Pattern pattern : swearsWhitelist) {
                if (pattern.matcher(swear).matches())
                    whitelisted = true;
            }

            if (!whitelisted) {
                words.add(swear);
                editedMessage = editedMessage.replaceAll(Pattern.quote(swear), replacement);
            }
        }

        return editedMessage;
    }

    @Override
    public boolean isBlocked() {
        return !getEditedMessage().equals(message);
    }

    private int[] getWord(String message, int start, int end) {
        int wordStart = 0;
        int wordEnd = message.length();

        char[] chars = message.toCharArray();
        for (int i = start; i >= 0; i--) {
            if (chars[i] == ' ') {
                wordStart = i + 1;
                break;
            }
        }

        for (int i = end; i < message.length(); i++) {
            if (chars[i] == ' ') {
                wordEnd = i;
                break;
            }
        }

        return new int[] {wordStart, wordEnd};
    }

}

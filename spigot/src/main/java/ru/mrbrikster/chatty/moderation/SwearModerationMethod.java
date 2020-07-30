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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwearModerationMethod extends ModifyingSubstringsModerationMethod {

    private final String replacement;
    private final List<String> words;
    @Getter private final boolean useBlock;

    private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static Pattern swearsPattern;
    private static List<Pattern> swearsWhitelist = new ArrayList<>();
    private static File swearsDirectory;
    private static File swearsFile;
    private static File whitelistFile;
    private String editedMessage;

    SwearModerationMethod(ConfigurationNode configurationNode, String message, String lastFormatColors) {
        super(message, lastFormatColors);

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

                pattern.append("|").append(swear);
            }

            SwearModerationMethod.swearsPattern = Pattern.compile(pattern.substring(1), PATTERN_FLAGS);
            Files.readLines(whitelistFile, StandardCharsets.UTF_8).forEach(SwearModerationMethod::addWhitelistWord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getWhitelistFile() {
        return whitelistFile;
    }

    public static void addWhitelistWord(String word) {
        swearsWhitelist.add(Pattern.compile(word.toLowerCase(Locale.ENGLISH), PATTERN_FLAGS));
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

            String lastColors = TextUtil.getLastColors(message.substring(0, previousWordStart));
            if (lastColors.isEmpty()) lastColors = lastFormatColors;

            boolean whitelisted = false;
            for (Pattern pattern : swearsWhitelist) {
                if (pattern.matcher(swear).matches())
                    whitelisted = true;
            }

            if (!whitelisted) {
                words.add(swear);
                editedMessage = editedMessage.replaceAll(Pattern.quote(swear), replacement + lastColors);
            }
        }

        return editedMessage;
    }

    @Override
    public boolean isBlocked() {
        return !getEditedMessage().equals(message);
    }

    @Override
    public String getLogPrefix() {
        return "SWEARS";
    }

    @Override
    public String getWarningMessageKey() {
        return "swear-found";
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

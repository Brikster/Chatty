package ru.brikster.chatty.convert.message;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyToMiniMessageConverter implements MessageConverter {

    private static final Pattern SIMPLE_COLOR_PATTERN = Pattern.compile("(?i)[§&][A-FK-OR\\d]");
    private static final Pattern HEX_GRADIENT_PATTERN = Pattern.compile("(?i)\\{#([A-F\\d]{6})(:#([A-F\\d]{6}))+( )([^{}])*(})");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)\\{#([A-F\\d]{6})}");
    private static final Pattern SPIGOT_HEX_COLOR_PATTERN = Pattern.compile("(?i)[§&]X([§&][A-F\\d]){6}");
    private static final Pattern COLOR_SYMBOLS_PATTERN = Pattern.compile("[§&]");

    private static final String RESET_TAGS = "<!bold><!italic><!strikethrough><!obfuscated><!underlined>";

    private static final Map<Character, String> legacyCodeToMiniMessageMap = new HashMap<Character, String>() {{
        put('a', RESET_TAGS + "<color:green>");
        put('b', RESET_TAGS + "<color:aqua>");
        put('c', RESET_TAGS + "<color:red>");
        put('d', RESET_TAGS + "<color:light_purple>");
        put('e', RESET_TAGS + "<color:yellow>");
        put('f', RESET_TAGS + "<color:white>");

        put('0', RESET_TAGS + "<color:dark_black>");
        put('1', RESET_TAGS + "<color:dark_blue>");
        put('2', RESET_TAGS + "<color:dark_green>");
        put('3', RESET_TAGS + "<color:dark_aqua>");
        put('4', RESET_TAGS + "<color:dark_red>");
        put('5', RESET_TAGS + "<color:dark_purple>");
        put('6', RESET_TAGS + "<color:gold>");
        put('7', RESET_TAGS + "<color:gray>");
        put('8', RESET_TAGS + "<color:dark_gray>");
        put('9', RESET_TAGS + "<color:blue>");

        put('k', "<obfuscated>");
        put('l', "<bold>");
        put('m', "<strikethrough>");
        put('n', "<underlined>");
        put('o', "<italic>");
        put('r', RESET_TAGS + "<color:white>");
    }};

    @Override
    public @NotNull String convert(@NotNull String message) {
        String convertedMessage;
        convertedMessage = convertHexLegacyGradients(message);
        convertedMessage = convertHexLegacyCodes(convertedMessage);
        convertedMessage = convertSpigotHexLegacyCodes(convertedMessage);
        convertedMessage = convertSimpleLegacyCodes(convertedMessage);
        return convertedMessage;
    }

    private @NotNull String convertHexLegacyGradients(@NotNull String message) {
        Matcher matcher = HEX_GRADIENT_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            String codes = group.substring(1, group.indexOf(' '));
            String text = group.substring(group.indexOf(' ') + 1, group.length() - 1);

            matcher.appendReplacement(buffer, RESET_TAGS + "<gradient:" + codes + ">" + text + "</gradient>");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private @NotNull String convertHexLegacyCodes(@NotNull String message) {
        Matcher matcher = HEX_COLOR_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, RESET_TAGS + "<color:#" + matcher.group().substring(2, 8) + ">");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private @NotNull String convertSpigotHexLegacyCodes(@NotNull String message) {
        Matcher matcher = SPIGOT_HEX_COLOR_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = COLOR_SYMBOLS_PATTERN.matcher(matcher.group()).replaceAll("").substring(1);
            matcher.appendReplacement(buffer, RESET_TAGS + "<color:#" + hex + ">");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private @NotNull String convertSimpleLegacyCodes(@NotNull String message) {
        Matcher matcher = SIMPLE_COLOR_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            String replacement = legacyCodeToMiniMessageMap.get(group.charAt(1));
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

}

package ru.brikster.chatty.convert.message;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyToMiniMessageConverter implements MessageConverter {

    private static final Pattern SIMPLE_COLOR_PATTERN = Pattern.compile("(?i)[§&][A-FK-OR\\d]");
    private static final Pattern HEX_GRADIENT_PATTERN = Pattern.compile("(?i)\\{#([A-F\\d]{6})(:#([A-F\\d]{6}))+( )([^{}])*(})");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)\\{#([A-F\\d]{6})}");
    private static final Pattern SPIGOT_HEX_COLOR_PATTERN = Pattern.compile("(?i)[§&]X([§&][A-F\\d]){6}");

    private static final Map<Character, String> legacyCodeToMiniMessageMap = new HashMap<Character, String>() {{
        put('a', "<reset><color:green>");
        put('b', "<reset><color:aqua>");
        put('c', "<reset><color:red>");
        put('d', "<reset><color:light_purple>");
        put('e', "<reset><color:yellow>");
        put('f', "<reset><color:white>");

        put('0', "<reset><color:dark_black>");
        put('1', "<reset><color:dark_blue>");
        put('2', "<reset><color:dark_green>");
        put('3', "<reset><color:dark_aqua>");
        put('4', "<reset><color:dark_red>");
        put('5', "<reset><color:dark_purple>");
        put('6', "<reset><color:gold>");
        put('7', "<reset><color:gray>");
        put('8', "<reset><color:dark_gray>");
        put('9', "<reset><color:blue>");

        put('k', "<obfuscated>");
        put('l', "<bold>");
        put('m', "<strikethrough>");
        put('n', "<underlined>");
        put('o', "<italic>");
        put('r', "<reset>");
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

            matcher.appendReplacement(buffer, "<reset><gradient:" + codes + ">" + text + "</gradient>");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private @NotNull String convertHexLegacyCodes(@NotNull String message) {
        Matcher matcher = HEX_COLOR_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "<reset><color:#" + matcher.group().substring(2, 8) + ">");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private @NotNull String convertSpigotHexLegacyCodes(@NotNull String message) {
        Matcher matcher = SPIGOT_HEX_COLOR_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group().replaceAll("[§&]", "").substring(1);
            matcher.appendReplacement(buffer, "<reset><color:#" + hex + ">");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private @NotNull String convertSimpleLegacyCodes(@NotNull String message) {
        Matcher matcher = SIMPLE_COLOR_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, legacyCodeToMiniMessageMap.get(matcher.group().charAt(1)));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

}

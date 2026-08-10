package ru.brikster.chatty.convert.message;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public final class LegacyToMiniMessageConverter implements MessageConverter {

    private static final Pattern BUKKIT_COLOR_PATTERN = Pattern.compile("(?i)[§&][A-FK-OR\\d]");
    private static final Pattern SPIGOT_HEX_COLOR_PATTERN = Pattern.compile("(?i)[§&]X([§&][A-F\\d]){6}");
    private static final Pattern PAPER_HEX_COLOR_PATTERN = Pattern.compile("(?i)[§&]#([A-F\\d]){6}");

    // Section-sign-only variants, used for text coming from other plugins
    private static final Pattern SECTION_BUKKIT_COLOR_PATTERN = Pattern.compile("(?i)§[A-FK-OR\\d]");
    private static final Pattern SECTION_SPIGOT_HEX_COLOR_PATTERN = Pattern.compile("(?i)§X(§[A-F\\d]){6}");
    private static final Pattern SECTION_PAPER_HEX_COLOR_PATTERN = Pattern.compile("(?i)§#([A-F\\d]){6}");

    // The "&" is optional: CMI and LuckPerms write {&#RRGGBB} where Chatty
    // writes {#RRGGBB}, and both reach the same colour.
    private static final Pattern CHATTY_HEX_COLOR_PATTERN = Pattern.compile("(?i)\\{&?#([A-F\\d]{6})}");
    private static final Pattern CHATTY_HEX_GRADIENT_PATTERN = Pattern.compile("(?i)\\{&?#([A-F\\d]{6})(:&?#([A-F\\d]{6}))+( )([^{}])*(})");

    // CMI spells a gradient by wrapping the text: {#RRGGBB>}text{#RRGGBB<}.
    // Extra {#RRGGBB>} markers inside become additional stops.
    private static final Pattern CMI_GRADIENT_PATTERN = Pattern.compile(
            "(?i)\\{&?#([A-F\\d]{6})>}(.*?)\\{&?#([A-F\\d]{6})<}", Pattern.DOTALL);
    private static final Pattern CMI_GRADIENT_STOP_PATTERN = Pattern.compile("(?i)\\{&?#([A-F\\d]{6})>}");

    private static final Pattern COLOR_SYMBOLS_PATTERN = Pattern.compile("[§&]");

    private static final String RESET_TAGS = "<!bold><!italic><!strikethrough><!obfuscated><!underlined>";

    private static final Map<Character, String> legacyCodeToMiniMessageMap = new HashMap<Character, String>() {{
        put('a', RESET_TAGS + "<color:green>");
        put('b', RESET_TAGS + "<color:aqua>");
        put('c', RESET_TAGS + "<color:red>");
        put('d', RESET_TAGS + "<color:light_purple>");
        put('e', RESET_TAGS + "<color:yellow>");
        put('f', RESET_TAGS + "<color:white>");

        put('0', RESET_TAGS + "<color:black>");
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
        convertedMessage = convertCmiGradients(message);
        convertedMessage = convertChattyCodes(convertedMessage);
        convertedMessage = convertChattyHexCodes(convertedMessage);
        convertedMessage = convertPaperHexCodes(convertedMessage, PAPER_HEX_COLOR_PATTERN);
        convertedMessage = convertSpigotHexCodes(convertedMessage, SPIGOT_HEX_COLOR_PATTERN);
        convertedMessage = convertBukkitCodes(convertedMessage, BUKKIT_COLOR_PATTERN);
        return convertedMessage;
    }

    /**
     * Converts only § (section sign) legacy codes, leaving &amp; codes and Chatty
     * {#...} codes untouched. Used for text that originates from other plugins:
     * Bukkit strips § from real player chat input, so any § reaching Chatty is
     * always foreign legacy formatting and is safe to convert. & codes are skipped
     * on purpose — they are gated behind the chatty.decoration permission.
     */
    public @NotNull String convertSectionCodes(@NotNull String message) {
        String convertedMessage;
        convertedMessage = convertPaperHexCodes(message, SECTION_PAPER_HEX_COLOR_PATTERN);
        convertedMessage = convertSpigotHexCodes(convertedMessage, SECTION_SPIGOT_HEX_COLOR_PATTERN);
        convertedMessage = convertBukkitCodes(convertedMessage, SECTION_BUKKIT_COLOR_PATTERN);
        // Drop any stray § that is not part of a recognised code, so the result
        // is always safe to hand to a strict MiniMessage parser.
        return convertedMessage.replace("§", "");
    }

    private @NotNull String convertPaperHexCodes(@NotNull String message, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group().substring(2);
            matcher.appendReplacement(builder, RESET_TAGS + "<color:#" + hex + ">");
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    private @NotNull String convertCmiGradients(@NotNull String message) {
        Matcher matcher = CMI_GRADIENT_PATTERN.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            StringBuilder stops = new StringBuilder("#").append(matcher.group(1));

            Matcher inner = CMI_GRADIENT_STOP_PATTERN.matcher(matcher.group(2));
            StringBuilder text = new StringBuilder();
            while (inner.find()) {
                stops.append(":#").append(inner.group(1));
                inner.appendReplacement(text, "");
            }
            inner.appendTail(text);

            stops.append(":#").append(matcher.group(3));

            matcher.appendReplacement(builder, Matcher.quoteReplacement(
                    RESET_TAGS + "<gradient:" + stops + ">" + text + "</gradient>"));
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    private @NotNull String convertChattyCodes(@NotNull String message) {
        Matcher matcher = CHATTY_HEX_GRADIENT_PATTERN.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String group = matcher.group();
            String codes = group.substring(1, group.indexOf(' '));
            String text = group.substring(group.indexOf(' ') + 1, group.length() - 1);

            matcher.appendReplacement(builder, Matcher.quoteReplacement(
                    RESET_TAGS + "<gradient:" + codes.replace("&", "") + ">" + text + "</gradient>"));
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    private @NotNull String convertChattyHexCodes(@NotNull String message) {
        Matcher matcher = CHATTY_HEX_COLOR_PATTERN.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, RESET_TAGS + "<color:#" + matcher.group(1) + ">");
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    private @NotNull String convertSpigotHexCodes(@NotNull String message, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String hex = COLOR_SYMBOLS_PATTERN.matcher(matcher.group()).replaceAll("").substring(1);
            matcher.appendReplacement(builder, RESET_TAGS + "<color:#" + hex + ">");
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    private @NotNull String convertBukkitCodes(@NotNull String message, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(message);

        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String group = matcher.group();
            String replacement = legacyCodeToMiniMessageMap.get(Character.toLowerCase(group.charAt(1)));
            matcher.appendReplacement(builder, replacement);
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

}

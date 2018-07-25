package ru.mrbrikster.chatty;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class Utils {

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)&([0-9A-F])");
    private static final Pattern MAGIC_PATTERN = Pattern.compile("(?i)&([K])");
    private static final Pattern BOLD_PATTERN = Pattern.compile("(?i)&([L])");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("(?i)&([M])");
    private static final Pattern UNDERLINE_PATTENT = Pattern.compile("(?i)&([N])");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?i)&([O])");
    private static final Pattern RESET_PATTERN = Pattern.compile("(?i)&([R])");
    private static final String PERMISSION_PREFIX = "chatty.style.";

    private static final Map<String, Pattern> PATTERNS = ImmutableMap.<String, Pattern>builder()
            .put(PERMISSION_PREFIX + "colors", COLOR_PATTERN)
            .put(PERMISSION_PREFIX + "magic", MAGIC_PATTERN)
            .put(PERMISSION_PREFIX + "bold", BOLD_PATTERN)
            .put(PERMISSION_PREFIX + "strikethrough", STRIKETHROUGH_PATTERN)
            .put(PERMISSION_PREFIX + "underline", UNDERLINE_PATTENT)
            .put(PERMISSION_PREFIX + "italic", ITALIC_PATTERN)
            .put(PERMISSION_PREFIX + "reset", RESET_PATTERN).build();

    private static final Pattern IP_PATTERN = Pattern.compile("((?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[.,-:; ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9]))");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%_\\+.~#?&/=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+~#?&/=]*)?");

    public static String colorize(String string) {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String stylish(Player player, String string, String chat) {
        for (Map.Entry<String, Pattern> entry : PATTERNS.entrySet()) {
            if (player.hasPermission(entry.getKey()) || player.hasPermission(entry.getKey() + "." + chat)) {
                string = entry.getValue().matcher(string).replaceAll("\u00A7$1");
            }
        }

        return string;
    }

    public static boolean containsIP(Chatty chatty, String message) {
        message = message.toLowerCase().replaceAll(" ", "");
        Matcher regexMatcher = IP_PATTERN.matcher(message);

        while (regexMatcher.find()) {
            if (regexMatcher.group().length() != 0) {
                String text = regexMatcher.group().trim()
                        .replaceAll("http://", "")
                        .replaceAll("https://", "")
                        .split("/")[0];

                if (text.split("\\.").length > 4) {
                    String[] domains = text.split("\\.");

                    int i = domains.length;
                    text = domains[--i] + "."
                            + domains[--i] + "."
                            + domains[--i] + "."
                            + domains[--i];
                }

                if (IP_PATTERN.matcher(text).find()) {
                    return false; //return !chatty.getConfiguration().getAdsWhitelist().contains(regexMatcher.group().trim());
                }
            }
        }

        return false;
    }

    public static boolean containsDomain(Chatty chatty, String message) {
        message = message.toLowerCase().replaceAll(" ", "");
        Matcher regexMatcher = DOMAIN_PATTERN.matcher(message);

        while (regexMatcher.find()) {
            if (regexMatcher.group().length() != 0) {
                String text = regexMatcher.group().trim()
                        .replaceAll("http://", "")
                        .replaceAll("https://", "")
                        .split("/")[0];

                if (text.split("\\.").length > 2) {
                    String[] domains = text.split("\\.");

                    int i = domains.length;
                    String zone = domains[--i];
                    String second = domains[--i];
                    text = second + "." + zone;
                }

                if (DOMAIN_PATTERN.matcher(text).find()) {
                    return false;//return !chatty.getConfiguration().getAdsWhitelist().contains(regexMatcher.group().trim());
                }
            }
        }

        return false;
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            Object onlinePlayers = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
            if (onlinePlayers instanceof Collection) {
                return (Collection<? extends Player>) onlinePlayers;
            } else return Arrays.asList((Player[]) onlinePlayers);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return Bukkit.getOnlinePlayers();
        }
    }

}

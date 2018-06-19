package ru.mrbrikster.chatty;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static String colorize(String string) {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<Player> getLocalRecipients(Player player, int distance, Chat chat) {
        Location location = player.getLocation();

        double squaredDistance = Math.pow(distance, 2);

        List<Player> players = new ArrayList<>(player.getWorld().getPlayers());

        return players.stream()
                .filter(recipient -> location.distanceSquared(recipient.getLocation()) < squaredDistance
                    && (recipient.equals(player) || !chat.isPermission()
                        || recipient.hasPermission("chatty.chat." + chat.getName() + ".see")
                        || recipient.hasPermission("chatty.chat." + chat.getName()))).collect(Collectors.toList());
    }

    public static String stylish(Player player, String string, String chat) {
        for (Map.Entry<String, Pattern> entry : PATTERNS.entrySet()) {
            if (player.hasPermission(entry.getKey()) || player.hasPermission(entry.getKey() + "." + chat)) {
                string = entry.getValue().matcher(string).replaceAll("\u00A7$1");
            }
        }

        return string;
    }

}

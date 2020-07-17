package ru.mrbrikster.chatty.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.util.textapi.ServerPackage;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TextUtil {

    private final Pattern HEX_COLORS_PATTERN = Pattern.compile("\\{#([a-fA-F0-9]{6})}");
    private final Pattern HEX_GRADIENT_PATTERN = Pattern.compile("\\{#([a-fA-F0-9]{6})(:#([a-fA-F0-9]{6}))+( )([^{}])*(})");
    private final Pattern HEX_SPIGOT_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]((?i)§[0-9A-F]){6}");

    private final List<ChatColor> FORMAT_COLORS = Arrays.asList(ChatColor.MAGIC, ChatColor.UNDERLINE, ChatColor.STRIKETHROUGH, ChatColor.BOLD, ChatColor.ITALIC, ChatColor.RESET);

    public boolean isColor(ChatColor color) {
        for (ChatColor formatColor : FORMAT_COLORS) {
            if (formatColor.equals(color)) {
                return false;
            }
        }

        return true;
    }

    public boolean isFormat(ChatColor color) {
        return !isColor(color);
    }

    /**
     * Removes spigot hex-codes from string
     * @param str string to strip hex
     * @return stripped string
     */
    public String stripHex(String str) {
        if (str == null) {
            return null;
        }

        Matcher matcher = HEX_SPIGOT_PATTERN.matcher(str);
        return matcher.replaceAll("");
    }

    /**
     * Finds simple and gradient hex patterns in string and converts it to Spigot format
     * @param str string to stylish
     * @return stylished string
     */
    public String stylish(String str) {
        if (str == null) {
            return null;
        }

        Matcher matcher = HEX_GRADIENT_PATTERN.matcher(str);

        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String gradient = matcher.group();

            int groups = 0;
            for (int i = 1; gradient.charAt(i) == '#'; i += 8) {
                groups++;
            }

            Color[] colors = new Color[groups];
            for (int i = 0; i < groups; i++) {
                colors[i] = ChatColor.of(gradient.substring((8 * i) + 1, (8 * i) + 8)).getColor();
            }

            String text = gradient.substring((groups - 1) * 8 + 9, gradient.length() - 1);

            char[] chars = text.toCharArray();

            StringBuilder gradientBuilder = new StringBuilder();

            int colorLength = chars.length / (colors.length - 1);
            int lastColorLength;
            if (colorLength == 0) {
                colorLength = 1;
                lastColorLength = 1;
                colors = Arrays.copyOfRange(colors, 0, chars.length);
            } else {
                lastColorLength = chars.length % (colorLength * (colors.length - 1)) + colorLength;
            }

            for (int i = 0; i < (colors.length - 1); i++) {
                int currentColorLength = ((i == colors.length - 2) ? lastColorLength : colorLength);
                for (int j = 0; j < currentColorLength; j++) {
                    Color color = calculateGradientColor(j + 1, currentColorLength, colors[i], colors[i + 1]);
                    ChatColor chatColor = ChatColor.of(color);

                    gradientBuilder.append(chatColor.toString()).append(chars[colorLength * i + j]);
                }
            }

            matcher.appendReplacement(stringBuffer, gradientBuilder.toString());
        }

        matcher.appendTail(stringBuffer);
        str = stringBuffer.toString();

        matcher = HEX_COLORS_PATTERN.matcher(str);
        stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String hexColorString = matcher.group();
            matcher.appendReplacement(stringBuffer, ChatColor.of(hexColorString.substring(1, hexColorString.length() - 1)).toString());
        }

        matcher.appendTail(stringBuffer);

        return ChatColor.translateAlternateColorCodes('&', stringBuffer.toString());
    }

    /**
     * Sends json-formatted message to player
     */
    public void sendJson(Player player, String json) {
        try {
            Class<?> clsIChatBaseComponent = ServerPackage.MINECRAFT.getClass("IChatBaseComponent");
            Class<?> clsChatMessageType = ServerPackage.MINECRAFT.getClass("ChatMessageType");
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Object chatBaseComponent = ServerPackage.MINECRAFT.getClass("IChatBaseComponent$ChatSerializer").getMethod("jsonToComponent", String.class).invoke(null, json);
            Object chatMessageType = clsChatMessageType.getMethod("valueOf", String.class).invoke(null, "CHAT");
            Object packetPlayOutChat;
            try {
                packetPlayOutChat = ServerPackage.MINECRAFT.getClass("PacketPlayOutChat").getConstructor(clsIChatBaseComponent, clsChatMessageType).newInstance(chatBaseComponent, chatMessageType);
            } catch (Throwable t) {
                // New constructor for v1_16
                packetPlayOutChat = ServerPackage.MINECRAFT.getClass("PacketPlayOutChat").getConstructor(clsIChatBaseComponent, clsChatMessageType, UUID.class).newInstance(chatBaseComponent, chatMessageType, null);
            }
            playerConnection.getClass().getMethod("sendPacket", ServerPackage.MINECRAFT.getClass("Packet")).invoke(playerConnection, packetPlayOutChat);
        } catch (Throwable e) {
            throw new RuntimeException("Json components is not supported by Chatty on your server version (" + ServerPackage.getServerVersion() + ")", e);
        }
    }

    private Color calculateGradientColor(int x, int parts, Color from, Color to) {
        double p = (double) (parts - x + 1) / (double) parts;

        return new Color(
                (int) (from.getRed() * p + to.getRed() * (1 - p)),
                (int) (from.getGreen() * p + to.getGreen() * (1 - p)),
                (int) (from.getBlue() * p + to.getBlue() * (1 - p))
        );
    }

}

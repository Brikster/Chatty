package ru.brikster.chatty.notification.advancement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class AdvancementJson {

    public static final String CRITERION = "chatty";

    private static final String CRITERIA = "\"criteria\":{\"" + CRITERION
            + "\":{\"trigger\":\"minecraft:impossible\"}},\"requirements\":[[\"" + CRITERION + "\"]]";

    /**
     * The parent every toast hangs from. It carries no display, and a root
     * without one gets no tab in the advancement screen - which is the point:
     * a toast registered as its own root makes a tab appear for as long as the
     * criterion is awarded, and blink away when it is revoked.
     */
    public static @NotNull String root() {
        return "{" + CRITERIA + "}";
    }

    /**
     * A toast normally shows the game's own wording for the frame above the
     * title. When the title splits into more than one line the client drops
     * that wording and shows the title's own lines instead, so a subtitle is
     * folded into the title behind a newline to claim both lines.
     */
    public static @NotNull Component heading(@NotNull Component title, @NotNull Component subtitle) {
        if (PlainTextComponentSerializer.plainText().serialize(subtitle).isEmpty()) {
            return title;
        }
        return Component.text().append(title).append(Component.newline()).append(subtitle).build();
    }

    /**
     * Identifies a toast by what it looks like, so an edited one registers
     * under a new key instead of colliding with the one already loaded.
     */
    public static @NotNull String fingerprint(@NotNull Component heading, @NotNull String icon,
                                              @NotNull AdvancementFrame frame) {
        String source = GsonComponentSerializer.gson().serialize(heading)
                + ' ' + icon + ' ' + frame.name();
        return Integer.toHexString(source.hashCode());
    }

    /**
     * @param modernIcon the icon's item moved from "item" to "id" in 1.20.5,
     *                   and the two shapes are not interchangeable
     */
    public static @NotNull String toast(@NotNull String parent, @NotNull Component heading,
                                        @NotNull String icon, @NotNull AdvancementFrame frame,
                                        boolean modernIcon) {
        return "{\"parent\":\"" + parent + "\","
                + "\"display\":{"
                + "\"icon\":{\"" + (modernIcon ? "id" : "item") + "\":\"" + icon + "\"},"
                + "\"title\":" + GsonComponentSerializer.gson().serialize(heading) + ","
                + "\"description\":{\"text\":\"\"},"
                + "\"frame\":\"" + frame.name().toLowerCase(Locale.ROOT) + "\","
                + "\"show_toast\":true,"
                + "\"announce_to_chat\":false,"
                + "\"hidden\":true},"
                + CRITERIA + "}";
    }

    /** Reduces a channel id to the characters a NamespacedKey accepts. */
    public static @NotNull String sanitize(@NotNull String id) {
        StringBuilder builder = new StringBuilder();
        for (char c : id.toLowerCase(Locale.ROOT).toCharArray()) {
            builder.append((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '.' ? c : '_');
        }
        return builder.toString();
    }

    private AdvancementJson() {
    }

}

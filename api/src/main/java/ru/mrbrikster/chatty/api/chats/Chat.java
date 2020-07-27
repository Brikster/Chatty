package ru.mrbrikster.chatty.api.chats;

import lombok.experimental.UtilityClass;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mrbrikster.chatty.json.FormattedMessage;

import java.util.Collection;
import java.util.function.Predicate;

public interface Chat {

    @UtilityClass
    class Ranges {
        public final int MULTI_SERVER = -3;
        public final int CROSS_WORLD = -2;
        public final int SINGLE_WORLD = -1;

        /**
         * Checks if range is applicable to messaging between two players
         * @return whether range is applicable or not
         */
        public boolean isApplicable(@NotNull Player firstPlayer, @NotNull Player secondPlayer, int range) {
            if (range == CROSS_WORLD || range == MULTI_SERVER) {
                return true;
            }

            World firstPlayerWorld = firstPlayer.getWorld();
            World secondPlayerWorld = secondPlayer.getWorld();
            if (range == SINGLE_WORLD) {
                return firstPlayerWorld.equals(secondPlayerWorld);
            }

            if (range >= 0) {
                return firstPlayerWorld.equals(secondPlayerWorld)
                        && firstPlayer.getLocation().distanceSquared(secondPlayer.getLocation()) <= (range * range);
            } else {
                return false;
            }
        }
    }

    /**
     * Name of chat from plugin configuration
     * @return name of chat
     */
    @NotNull
    String getName();

    /**
     * Chat format has the following variables:
     * 1) {player} - player nickname
     * 2) {prefix}, {suffix} - prefix and suffix of player
     * 3) %<placeholder>% - various placeholders from PlaceholderAPI
     *
     * Also chat format supports own color formats (1.16+):
     * {#hexhex}text - for plain hex-colored strings
     * {#hexhex:#hexhex:#hexhex... text} - for gradient-colored strings
     *
     * @return chat format specified in configuration
     */
    @NotNull
    String getFormat();

    /**
     * Range param for the chat messages
     * -3 is used for multi-server messaging (when "general.bungeecord" is true)
     * -2 is used for cross-world chats
     * -1 is for global single-world chats
     * 0 and higher for ranged local-chats
     *
     * @see Ranges#isApplicable(Player, Player, int)
     *
     * @return range value for this chat
     */
    int getRange();

    /**
     * Permission requiring can be disable in configuration
     * If permission is enable, player must has "chatty.chat.<chat>" permission to use it
     * @return whether permission required or not
     */
    boolean isPermissionRequired();

    /**
     * Creates collection of online players chat can see message from this chat
     *
     * @param player player who sends a message. Can be null only if chat chat range <= -2
     * @return collection of chat recipients
     */
    @NotNull
    Collection<? extends Player> getRecipients(@Nullable Player player);

    /**
     * Filters collection of online players chat can see message from this chat
     *
     * @param player player who sends a message. Can be null only if chat chat range <= -2
     * @param players collection of players to filter
     * @return edited collection of chat recipients
     */
    @NotNull
    Collection<? extends Player> filterRecipients(@Nullable Player player, @NotNull Collection<? extends Player> players);

    /**
     * This method let you send any message to the chat participants (without {@link Chat#getFormat()})
     * Message will be processed with {@link ru.mrbrikster.chatty.util.TextUtil#stylish(String)}
     *
     * Messages supports Chatty stylish formats (1.16+):
     * {#hexhex}text - for plain hex-colored strings
     * {#hexhex:#hexhex:#hexhex... text} - for gradient-colored strings
     *
     * @param message message to send
     */
    default void sendMessage(String message) {
        sendMessage(message, (player -> true));
    }

    /**
     * This method let you send any message to the chat participants (without {@link Chat#getFormat()})
     * Message will be processed with {@link ru.mrbrikster.chatty.util.TextUtil#stylish(String)}
     *
     * Messages supports Chatty stylish formats (1.16+):
     * {#hexhex}text - for plain hex-colored strings
     * {#hexhex:#hexhex:#hexhex... text} - for gradient-colored strings
     *
     *
     * @param message message to send
     * @param playerPredicate predicate for message recipient
     */
    void sendMessage(String message, Predicate<Player> playerPredicate);

    /**
     * This method let you send {@link FormattedMessage} to the chat participants (without {@link Chat#getFormat()})
     * {@link FormattedMessage} object let you create various JSON-formatted messages,
     * make hover tooltips, clickable links etc.
     *
     * @param formattedMessage message to send
     */
    default void sendFormattedMessage(FormattedMessage formattedMessage) {
        sendFormattedMessage(formattedMessage, (player -> true));
    }

    /**
     * This method let you send {@link FormattedMessage} to the chat participants (without {@link Chat#getFormat()})
     * {@link FormattedMessage} object let you create various JSON-formatted messages,
     * make hover tooltips, clickable links etc.
     *
     * @param formattedMessage message to send
     * @param playerPredicate predicate for message recipient
     */
    void sendFormattedMessage(FormattedMessage formattedMessage, Predicate<Player> playerPredicate);

}

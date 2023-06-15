package ru.brikster.chatty.api.chat;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.command.ChatCommand;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.range.Ranges;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public interface Chat {

    /**
     * Name of chat from plugin configuration
     *
     * @return name of chat
     */
    @NotNull
    String getName();

    @NotNull
    String getDisplayName();

    /**
     * Chat format has the following variables:
     * 1) {player} - player nickname
     * 2) {prefix}, {suffix} - prefix and suffix of player
     * 3) %<placeholder>% - various placeholders from PlaceholderAPI
     *
     * @return chat format specified in configuration
     */
    @NotNull
    Component getFormat();

    @NotNull String getSymbol();

    @Nullable ChatCommand getCommand();

    default boolean isSymbolAllowed() {
        return !getSymbol().isEmpty();
    }

    default boolean isCommandAllowed() {
        return getCommand() != null;
    }

    @NotNull Set<@NotNull MessageTransformStrategy<?>> getStrategies();

    void addStrategy(@NotNull MessageTransformStrategy<?> strategy);

    boolean removeStrategy(@NotNull MessageTransformStrategy<?> strategy);

    /**
     * Range param for the chat messages
     * -3 is used for multiserver messaging (when "general.bungeecord" is true)
     * -2 is used for cross-world chats
     * -1 is for global single-world chats
     * 0 and higher for ranged local-chats
     *
     * @return range value for this chat
     * @see Ranges#isApplicable(Player, Player, int)
     */
    int getRange();

    /**
     * Permission requiring can be disable in configuration
     * If permission is enable, player must has "chatty.chat.<chat>" permission to use it
     *
     * @return whether permission required or not
     */
    boolean isPermissionRequired();

    boolean hasSymbolWritePermission(Player sender);

    boolean hasCommandWritePermission(Player sender);

    boolean hasReadPermission(Player sender);

    @NotNull
    Predicate<Player> getRecipientPredicate(@Nullable Player sender);

    /**
     * Creates collection of online players chat can see message from this chat
     *
     * @param sender message sender
     * @return collection of chat recipients
     */
    @NotNull
    default Collection<? extends Player> getRecipients(@Nullable Player sender) {
        Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        onlinePlayers.removeIf(player -> !getRecipientPredicate(sender).test(player));
        return Collections.unmodifiableCollection(onlinePlayers);
    }

    /**
     * This method let you send any message to the chat participants (without {@link Chat#getFormat()})
     *
     * @param audienceProvider audience provider to get audience
     * @param message message to send
     */
    default void sendMessage(BukkitAudiences audienceProvider, Component message) {
        sendMessage(audienceProvider, message, (recipient -> true));
    }

    /**
     * This method let you send any message to the chat participants (without {@link Chat#getFormat()})
     *
     * @param audienceProvider audience provider to get audience
     * @param message            message to send
     * @param recipientPredicate predicate for message recipient
     */
    void sendMessage(BukkitAudiences audienceProvider, Component message, Predicate<CommandSender> recipientPredicate);

}

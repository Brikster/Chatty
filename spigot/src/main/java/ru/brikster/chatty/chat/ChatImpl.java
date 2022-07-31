package ru.brikster.chatty.chat;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.command.ChatCommand;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.range.Ranges;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ChatImpl implements Chat {

    @Getter
    private final @NotNull String name;
    private final @Nullable String displayName;

    @Getter
    private final @NotNull Component format;

    @Getter
    private final @NotNull String symbol;

    @Getter
    private final @Nullable ChatCommand command;

    @Getter
    private final int range;

    @Getter
    private final boolean permissionRequired;

    private final List<MessageTransformStrategy<?, ?>> strategies
            = new ArrayList<>();

    @Override
    public @NotNull String getDisplayName() {
        return displayName == null
                ? name
                : displayName;
    }

    @Override
    public @NotNull List<@NotNull MessageTransformStrategy<?, ?>> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    @Override
    public void addStrategy(int index, @NotNull MessageTransformStrategy<?, ?> strategy) {
        strategies.add(index, strategy);
    }

    @Override
    public void addStrategy(@NotNull MessageTransformStrategy<?, ?> strategy) {
        strategies.add(strategy);
    }

    @Override
    public MessageTransformStrategy<?, ?> removeStrategy(int index) {
        return strategies.remove(index);
    }

    @Override
    public boolean removeStrategy(@NotNull MessageTransformStrategy<?, ?> strategy) {
        return strategies.remove(strategy);
    }

    @Override
    public boolean hasSymbolWritePermission(Player sender) {
        return sender.hasPermission("chatty.chat." + getName())
                || sender.hasPermission("chatty.chat." + getName() + ".write")
                || sender.hasPermission("chatty.chat." + getName() + ".send")
                || sender.hasPermission("chatty.chat." + getName() + ".write.symbol")
                || sender.hasPermission("chatty.chat." + getName() + ".send.symbol");
    }

    @Override
    public boolean hasCommandWritePermission(Player sender) {
        return sender.hasPermission("chatty.chat." + getName())
                || sender.hasPermission("chatty.chat." + getName() + ".write")
                || sender.hasPermission("chatty.chat." + getName() + ".send")
                || sender.hasPermission("chatty.chat." + getName() + ".write.command")
                || sender.hasPermission("chatty.chat." + getName() + ".send.command");
    }

    @Override
    public boolean hasReadPermission(Player sender) {
        return sender.hasPermission("chatty.chat." + getName())
                || sender.hasPermission("chatty.chat." + getName() + ".read")
                || sender.hasPermission("chatty.chat." + getName() + ".see");
    }

    @Override
    public @NotNull Predicate<Player> getRecipientPredicate(@Nullable Player sender) {
        return player -> {
            if (player.equals(sender)) {
                return true;
            }

            if (isPermissionRequired() && !hasReadPermission(player)) {
                return false;
            }

            return sender == null || Ranges.isApplicable(sender, player, range);
        };
    }

    @Override
    public void sendMessage(Component message, Predicate<CommandSender> recipientPredicate) {
        BukkitAudiences.create(Chatty.get())
                .filter(recipientPredicate)
                .sendMessage(message);
    }

}

package ru.brikster.chatty.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.ChatStyle;
import ru.brikster.chatty.api.chat.command.ChatCommand;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.range.Ranges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@RequiredArgsConstructor
public final class ChatImpl implements Chat {

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

    @Getter
    private final Set<ChatStyle> styles;

    @Getter
    private final boolean sendNobodyHeardYou;

    @Getter
    private final boolean parseLinks;

    @Getter
    private final boolean enableSpy;

    @Getter
    private final Sound sound;

    @Getter
    private final Component spyFormat;

    @Getter
    private final int cooldown;

    private final List<MessageTransformStrategy<?>> strategies
            = new ArrayList<>();


    @Override
    public @NotNull String getDisplayName() {
        return displayName == null
                ? name
                : displayName;
    }

    @Override
    public @NotNull List<@NotNull MessageTransformStrategy<?>> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    @Override
    public void addStrategy(@NotNull MessageTransformStrategy<?> strategy) {
        strategies.add(strategy);
    }

    @Override
    public boolean removeStrategy(@NotNull MessageTransformStrategy<?> strategy) {
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
    public void sendLegacyMessage(Plugin plugin, String message, Predicate<CommandSender> recipientPredicate) {
        var component = LegacyComponentSerializer.legacySection().deserialize(message);
        sendComponent(plugin, component, recipientPredicate);
    }

    @Override
    public void sendJsonComponent(Plugin plugin, String componentJson, Predicate<CommandSender> recipientPredicate) {
        var component = GsonComponentSerializer.gson().deserialize(componentJson);
        sendComponent(plugin, component, recipientPredicate);
    }

    private void sendComponent(Plugin plugin, Component component, Predicate<CommandSender> recipientPredicate) {
        @SuppressWarnings("resource")
        var audience = BukkitAudiences
                .create(plugin)
                .filter(sender -> {
                    if (sender instanceof Player) {
                        boolean test = getRecipientPredicate(null).test((Player) sender);
                        if (!test) return false;
                    }
                    return recipientPredicate.test(sender);
                });
        audience.sendMessage(component);
    }

}

package ru.mrbrikster.chatty.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Chat implements ru.mrbrikster.chatty.api.chats.Chat {

    private static final String CHAT_COOLDOWN_METADATA_KEY = "chatty.cooldown.chat.%s";

    @Getter private final String name;
    @Getter private final boolean enable;
    @Getter private final String format;
    @Getter private final int range;
    @Getter private final String symbol;
    @Getter private final boolean permission;
    @Getter private final long cooldown;
    @Getter private final int money;
    @Getter private final String command;
    @Getter private final List<String> aliases;

    @Getter @Setter private BukkitCommand bukkitCommand;

    public Chat(String name, boolean enable, String format, int range,
                String symbol, boolean permission, long cooldown, int money,
                String command, List<String> aliases) {
        this.name = name;
        this.enable = enable;
        this.format = format;
        this.range = range;
        this.symbol = symbol == null ? "" : symbol;
        this.permission = permission;
        this.cooldown = cooldown * 1000;
        this.money = money;
        this.command = command;
        this.aliases = aliases;
    }

    public boolean isAllowed(Player player) {
        return !isPermission()
                || player.hasPermission(String.format("chatty.chat.%s", getName()))
                || player.hasPermission(String.format("chatty.chat.%s.write", getName()));
    }

    void setCooldown(Player player) {
        player.setMetadata(String.format(CHAT_COOLDOWN_METADATA_KEY, name),
                new FixedMetadataValue(Chatty.instance(), System.currentTimeMillis()));
    }

    long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata(String.format(CHAT_COOLDOWN_METADATA_KEY, name));

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

    @Override
    public boolean isPermissionRequired() {
        return permission;
    }

    @Override
    @NotNull
    public Collection<? extends Player> getRecipients(@Nullable Player player) {
        List<Player> players = new ArrayList<>(Reflection.getOnlinePlayers());

        if (range > -2) {
            if (player == null) {
                return Collections.emptySet();
            }

            players.removeIf(onlinePlayer -> !onlinePlayer.getWorld().equals(player.getWorld()));
        }

        return players.stream()
                .filter(recipient -> {
                    if (player == null) {
                        return true;
                    }

                    JsonElement jsonElement = Chatty.instance().storage().getProperty(recipient, "ignore").orElseGet(JsonArray::new);

                    if (!jsonElement.isJsonArray())
                        jsonElement = new JsonArray();

                    for (JsonElement ignoreJsonElement : jsonElement.getAsJsonArray()) {
                        if (player.getName().equalsIgnoreCase(ignoreJsonElement.getAsString())) {
                            return false;
                        }
                    }

                    return true;
                })
                .filter(recipient -> {
                    if (player == null) {
                        return true;
                    }

                    return Ranges.isApplicable(recipient, player, range);
                })
                .filter(recipient ->
                        (recipient.equals(player) || !permission
                                || recipient.hasPermission("chatty.chat." + name + ".see")
                                || recipient.hasPermission("chatty.chat." + name))).collect(Collectors.toList());
    }

    @Override
    public void sendMessage(String message, Predicate<Player> playerPredicate) {
        String stylishedMessage = TextUtil.stylish(message);
        getRecipients(null).stream().filter(playerPredicate).forEach(player ->
                player.sendMessage(stylishedMessage));

        Bukkit.getConsoleSender().sendMessage(TextUtil.stripHex(stylishedMessage));
    }

    @Override
    public void sendFormattedMessage(FormattedMessage formattedMessage, Predicate<Player> playerPredicate) {
        formattedMessage.send(getRecipients(null).stream().filter(playerPredicate).collect(Collectors.toSet()));
        formattedMessage.sendConsole();
    }

}

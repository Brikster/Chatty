package ru.mrbrikster.chatty.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Getter
public class Chat implements ru.mrbrikster.chatty.api.chats.Chat {

    private static final String CHAT_COOLDOWN_METADATA_KEY = "chatty.cooldown.chat.%s";

    @NotNull private final String name;
    private final boolean enable;
    @NotNull private final String format;
    private final int range;
    @NotNull private final String symbol;
    private final boolean permissionRequired;
    private final long cooldown;
    private final int money;
    @Nullable private final String command;
    @Nullable private final List<String> aliases;

    @Nullable private final Sound sound;

    private final boolean spyEnabled;

    private final boolean capsModerationEnabled;
    private final boolean swearModerationEnabled;
    private final boolean advertisementModerationEnabled;

    @Setter private BukkitCommand bukkitCommand;

    public boolean isWriteAllowed(Player player) {
        return !isPermissionRequired() || player.hasPermission(String.format("chatty.chat.%s.write", getName()));
    }

    void setCooldown(Player player) {
        player.setMetadata(String.format(CHAT_COOLDOWN_METADATA_KEY, name),
                new FixedMetadataValue(Chatty.instance(), System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata(String.format(CHAT_COOLDOWN_METADATA_KEY, name));

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + (this.cooldown * 1000) - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

    @Override
    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    @Override
    @NotNull
    public Collection<? extends Player> getRecipients(@Nullable Player player) {
        return filterRecipients(player, new ArrayList<>(Reflection.getOnlinePlayers()));
    }

    @Override
    @NotNull
    public Collection<? extends Player> filterRecipients(@Nullable Player player, @NotNull Collection<? extends Player> players) {
        if (range > -2) {
            if (player == null) {
                players.clear();
                return players;
            }

            players.removeIf(onlinePlayer -> !onlinePlayer.getWorld().equals(player.getWorld()));
        }

        if (player != null) {
            players.removeIf(recipient -> {
                JsonElement jsonElement = Chatty.instance().storage().getProperty(recipient, "ignore").orElseGet(JsonArray::new);

                if (jsonElement.isJsonArray()) {
                    for (JsonElement ignoreJsonElement : jsonElement.getAsJsonArray()) {
                        if (player.getName().equalsIgnoreCase(ignoreJsonElement.getAsString())) {
                            return true;
                        }
                    }
                }

                return false;
            });
            players.removeIf(recipient -> !Ranges.isApplicable(recipient, player, range));
        }

        players.removeIf(recipient ->
                !(recipient.equals(player)
                        || !isPermissionRequired()
                        || recipient.hasPermission("chatty.chat." + name + ".see"))
        );

        return players;
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

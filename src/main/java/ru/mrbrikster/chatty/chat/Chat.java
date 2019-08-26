package ru.mrbrikster.chatty.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chat {

    private static final String COOLDOWN_CHAT_FORMAT = "chatty.cooldown.chat.%s";
    @Getter private final String name;
    @Getter private final boolean enable;
    @Getter private final String format;
    @Getter private final int range;
    @Getter private final String symbol;
    @Getter private final boolean permission;
    @Getter private final long cooldown;
    @Getter private final int money;

    public Chat(String name, boolean enable, String format, int range, String symbol, boolean permission, long cooldown, int money) {
        this.name = name.toLowerCase();
        this.enable = enable;
        this.format = format;
        this.range = range;
        this.symbol = symbol == null ? "" : symbol;
        this.permission = permission;
        this.cooldown = cooldown * 1000;
        this.money = money;
    }

    public List<Player> getRecipients(Player player, JsonStorage jsonStorage) {
        Location location = player.getLocation();

        double squaredDistance = Math.pow(range, 2);

        List<Player> players = new ArrayList<>(Reflection.getOnlinePlayers());

        if (range > -2)
            players.removeIf(onlinePlayer -> !onlinePlayer.getWorld().equals(player.getWorld()));

        return players.stream()
                .filter(recipient -> {
                    JsonElement jsonElement = jsonStorage.getProperty(recipient, "ignore").orElseGet(JsonArray::new);

                    if (!jsonElement.isJsonArray())
                        jsonElement = new JsonArray();

                    for (JsonElement ignoreJsonElement : jsonElement.getAsJsonArray()) {
                        if (player.getName().equals(ignoreJsonElement.getAsString()))
                            return false;
                    }

                    return true;
                })
                .filter(recipient ->
                        (range <= -1 || location.distanceSquared(recipient.getLocation()) < squaredDistance)
                                && (recipient.equals(player) || !permission
                                || recipient.hasPermission("chatty.chat." + name + ".see")
                                || recipient.hasPermission("chatty.chat." + name))).collect(Collectors.toList());
    }

    public void setCooldown(Player player) {
        player.setMetadata(String.format(COOLDOWN_CHAT_FORMAT, name),
                new FixedMetadataValue(Chatty.instance(), System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata(String.format(COOLDOWN_CHAT_FORMAT, name));

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

}

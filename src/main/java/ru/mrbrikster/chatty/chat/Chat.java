package ru.mrbrikster.chatty.chat;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chat {

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

    public List<Player> getRecipients(Player player) {
        Location location = player.getLocation();

        double squaredDistance = Math.pow(range, 2);

        List<Player> players = new ArrayList<>(range > -2 ? player.getWorld().getPlayers() : Utils.getOnlinePlayers());

        return players.stream()
                .filter(recipient ->
                        (range <= -1 || location.distanceSquared(recipient.getLocation()) < squaredDistance)
                        && (recipient.equals(player) || !permission
                        || recipient.hasPermission(String.format("chatty.chat.%s.see", name))
                        || recipient.hasPermission(String.format("chatty.chat.%s", name)))).collect(Collectors.toList());
    }

    public void setCooldown(Player player) {
        player.setMetadata(String.format("chatty.cooldown.chat.%s", name), new FixedMetadataValue(Chatty.instance(), System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata(String.format("chatty.cooldown.chat.%s", name));

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

}

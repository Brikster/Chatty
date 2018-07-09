package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class Chat {

    @Getter private final String name;
    @Getter private final boolean enable;
    @Getter private final String format;
    @Getter private final int range;
    @Getter private final String symbol;
    @Getter private final boolean permission;
    @Getter private final long cooldown;
    @Getter private final int money;

    Chat(String name, boolean enable, String format, int range, String symbol, boolean permission, long cooldown, int money) {
        this.name = name.toLowerCase();
        this.enable = enable;
        this.format = format;
        this.range = range;
        this.symbol = symbol == null ? "" : symbol;
        this.permission = permission;
        this.cooldown = cooldown * 1000;
        this.money = money;
    }

    public void setCooldown(Main main, Player player) {
        player.setMetadata("chatty.cooldown.chat." + this.name, new FixedMetadataValue(main, System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata("chatty.cooldown.chat." + this.name);

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

}

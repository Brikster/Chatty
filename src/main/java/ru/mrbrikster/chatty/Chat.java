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
    @Getter private final long cooldown;

    Chat(String name, boolean enable, String format, int range, String symbol, long cooldown) {
        this.name = name.toLowerCase();
        this.enable = enable;
        this.format = format;
        this.range = range;
        this.symbol = symbol == null ? "" : symbol;
        this.cooldown = cooldown * 1000;
    }

    public void setCooldown(Main main, Player player) {
        player.setMetadata("chatty.cooldown." + this.name, new FixedMetadataValue(main, System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata("chatty.cooldown." + this.name);

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

}

package ru.mrbrikster.chatty.commands;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import ru.mrbrikster.chatty.Main;

import java.util.ArrayList;
import java.util.List;

public class CommandGroup {

    @Getter private final String name;
    @Getter private final List<Trigger> triggers;
    @Getter private final boolean block;
    @Getter private final String message;
    @Getter private final long cooldown;

    public CommandGroup(String name, List<String> triggers, boolean block, String message, long cooldown) {
        this.name = name;
        this.triggers = parseTriggers(triggers);
        this.block = block;
        this.message = message;
        this.cooldown = cooldown * 1000;
    }

    public void setCooldown(Main main, Player player) {
        player.setMetadata("chatty.cooldown.command." + this.name, new FixedMetadataValue(main, System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata("chatty.cooldown.command." + this.name);

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

    private List<Trigger> parseTriggers(List<String> triggers) {
        List<Trigger> triggerList = new ArrayList<>();

        for (String trigger : triggers) {
            String[] split = trigger.split(" ", 2);

            String type = split[0].toUpperCase();
            String data = split[1];

            triggerList.add(new Trigger(Trigger.Type.valueOf(type), data));
        }

        return triggerList;
    }

    public static class Trigger {

        @Getter private final Type type;
        @Getter private final String data;

        Trigger(Type type, String data) {
            this.type = type;
            this.data = data;
        }

        public enum Type {
            CONTAINS,
            STARTS,
            COMMAND
        }

    }
}

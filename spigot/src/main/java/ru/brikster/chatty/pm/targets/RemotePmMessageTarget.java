package ru.brikster.chatty.pm.targets;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

@RequiredArgsConstructor
public final class RemotePmMessageTarget implements PmMessageTarget {

    private final String targetName;
    private final UUID targetUuid;

    @Override
    public UUID getUuid() {
        return targetUuid;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public String getName() {
        return targetName;
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        return Bukkit.getOfflinePlayer(targetUuid);
    }

    @Override
    public CommandSender asCommandSender() {
        throw new IllegalStateException("Cannot cast remote target to CommandSender");
    }

}

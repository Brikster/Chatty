package ru.brikster.chatty.pm.targets;

import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public final class CommandSenderPmMessageTarget implements PmMessageTarget {

    private final CommandSender commandSender;

    @Override
    public UUID getUuid() {
        return commandSender instanceof Player
                ? ((Player) commandSender).getUniqueId()
                : null;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isConsole() {
        return commandSender instanceof ConsoleCommandSender;
    }

    @Override
    public String getName() {
        return commandSender.getName();
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        return (OfflinePlayer) commandSender;
    }

    @Override
    public CommandSender asCommandSender() {
        return commandSender;
    }

}

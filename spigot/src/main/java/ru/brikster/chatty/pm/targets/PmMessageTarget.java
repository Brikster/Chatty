package ru.brikster.chatty.pm.targets;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public interface PmMessageTarget {

    UUID getUuid();

    boolean isOnline();

    boolean isConsole();

    String getName();

    OfflinePlayer asOfflinePlayer();

    CommandSender asCommandSender();

}

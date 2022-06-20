package ru.brikster.chatty.prefix;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface PrefixProvider {

    @Nullable String getPrefix(Player player);

    @Nullable String getSuffix(Player player);

}

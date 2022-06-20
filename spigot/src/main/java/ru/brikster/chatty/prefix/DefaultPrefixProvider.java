package ru.brikster.chatty.prefix;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DefaultPrefixProvider implements PrefixProvider {

    @Override
    public @Nullable String getPrefix(Player player) {
        return null;
    }

    @Override
    public @Nullable String getSuffix(Player player) {
        return null;
    }

}

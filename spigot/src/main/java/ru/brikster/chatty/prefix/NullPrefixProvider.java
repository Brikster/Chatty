package ru.brikster.chatty.prefix;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;

@Singleton
public final class NullPrefixProvider implements PrefixProvider {

    @Override
    public @Nullable String getPrefix(OfflinePlayer player) {
        return null;
    }

    @Override
    public @Nullable String getSuffix(OfflinePlayer player) {
        return null;
    }

}

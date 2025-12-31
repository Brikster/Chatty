package ru.brikster.chatty.prefix;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.inject.Singleton;

@Singleton
public final class LuckpermsPrefixProvider implements PrefixProvider {

    private final LuckPerms luckPerms;
    private final PlayerAdapter<Player> playerAdapter;

    public LuckpermsPrefixProvider() {
        var registration = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        this.luckPerms = registration != null ? registration.getProvider() : null;
        this.playerAdapter = this.luckPerms != null ? this.luckPerms.getPlayerAdapter(Player.class) : null;
    }

    @Override
    public String getPrefix(OfflinePlayer player) {
        if (!(player instanceof Player)) {
            return null;
        }
        if (playerAdapter == null) {
            return null;
        }
        User user = playerAdapter.getUser((Player) player);
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Override
    public String getSuffix(OfflinePlayer player) {
        if (!(player instanceof Player)) {
            return null;
        }
        if (playerAdapter == null) {
            return null;
        }
        User user = playerAdapter.getUser((Player) player);
        return user.getCachedData().getMetaData().getSuffix();
    }

}

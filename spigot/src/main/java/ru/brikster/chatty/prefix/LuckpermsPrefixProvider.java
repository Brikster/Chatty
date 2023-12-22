package ru.brikster.chatty.prefix;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class LuckpermsPrefixProvider implements PrefixProvider {

    private final LuckPerms luckPerms = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(LuckPerms.class))
            .getProvider();
    private final PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);

    @Override
    public String getPrefix(OfflinePlayer player) {
        if (!(player instanceof Player)) {
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
        User user = playerAdapter.getUser((Player) player);
        return user.getCachedData().getMetaData().getSuffix();
    }

}

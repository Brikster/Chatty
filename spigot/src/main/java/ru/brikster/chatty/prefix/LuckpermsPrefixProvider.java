package ru.brikster.chatty.prefix;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class LuckpermsPrefixProvider implements PrefixProvider {

    private final LuckPerms luckPerms = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(LuckPerms.class))
            .getProvider();
    private final PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);

    @Override
    public String getPrefix(Player player) {
        User user = playerAdapter.getUser(player);
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Override
    public String getSuffix(Player player) {
        User user = playerAdapter.getUser(player);
        return user.getCachedData().getMetaData().getSuffix();
    }

}

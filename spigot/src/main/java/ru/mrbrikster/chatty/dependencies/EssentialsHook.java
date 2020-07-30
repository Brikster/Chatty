package ru.mrbrikster.chatty.dependencies;

import com.earth2me.essentials.Essentials;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;

public class EssentialsHook {

    private final Essentials essentials;

    public EssentialsHook(Chatty chatty) {
        this.essentials = ((Essentials) chatty.getServer().getPluginManager().getPlugin("Essentials"));
    }

    public boolean isVanished(Player player) {
        return essentials.getUser(player).isVanished();
    }

}

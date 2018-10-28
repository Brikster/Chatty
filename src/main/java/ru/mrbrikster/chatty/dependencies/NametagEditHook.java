package ru.mrbrikster.chatty.dependencies;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;

public class NametagEditHook {

    public void setPrefix(Player player, String prefix) {
        if (prefix == null) {
            String suffix = NametagEdit.getApi().getNametag(player).getSuffix();
            NametagEdit.getApi().reloadNametag(player);

            Bukkit.getScheduler().runTaskLater(Chatty.instance(), () ->
                    NametagEdit.getApi().setSuffix(player, suffix), 10L);
        } else NametagEdit.getApi().setPrefix(player, prefix);
    }

    public void setSuffix(Player player, String suffix) {
        if (suffix == null) {
            String prefix = NametagEdit.getApi().getNametag(player).getPrefix();
            NametagEdit.getApi().reloadNametag(player);

            Bukkit.getScheduler().runTaskLater(Chatty.instance(), () ->
                    NametagEdit.getApi().setPrefix(player, prefix), 10L);
        } else NametagEdit.getApi().setSuffix(player, suffix);
    }

}

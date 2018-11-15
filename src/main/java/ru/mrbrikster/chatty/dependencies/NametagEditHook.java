package ru.mrbrikster.chatty.dependencies;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.events.NametagFirstLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.PermanentStorage;

public class NametagEditHook implements Listener {

    private final Configuration configuration;
    private final PermanentStorage permanentStorage;

    NametagEditHook(Configuration configuration,
                    PermanentStorage permanentStorage) {
        this.configuration = configuration;
        this.permanentStorage = permanentStorage;

        Bukkit.getPluginManager().registerEvents(this, Chatty.instance());
    }

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

    @EventHandler
    public void onNametagFirstLoadedEvent(NametagFirstLoadedEvent nametagFirstLoadedEvent) {
        Player player = nametagFirstLoadedEvent.getPlayer();

        if (configuration.getNode("general.prefix-command.enable").getAsBoolean(false)
                && configuration.getNode("general.prefix-command.auto-nte").getAsBoolean(false)) {
            permanentStorage.getProperty(player, "prefix").ifPresent(prefix -> setPrefix(player, prefix.getAsString()));
        }

        if (configuration.getNode("general.suffix-command.enable").getAsBoolean(false)
                && configuration.getNode("general.suffix-command.auto-nte").getAsBoolean(false)) {
            permanentStorage.getProperty(player, "suffix").ifPresent(suffix -> setSuffix(player, suffix.getAsString()));
        }
    }

}

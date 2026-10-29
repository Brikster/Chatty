package ru.mrbrikster.chatty.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.lang.reflect.Method;

/**
 * NametagEdit is reached through reflection: its Maven repository is gone,
 * so the plugin cannot be a compile-time dependency anymore.
 */
public class NametagEditHook implements Listener {

    private final Configuration configuration;
    private final JsonStorage jsonStorage;

    private final Object api;
    private final Method getNametagMethod;
    private final Method reloadNametagMethod;
    private final Method setPrefixMethod;
    private final Method setSuffixMethod;

    NametagEditHook(Configuration configuration,
                    JsonStorage jsonStorage) throws ReflectiveOperationException {
        this.configuration = configuration;
        this.jsonStorage = jsonStorage;

        this.api = Class.forName("com.nametagedit.plugin.NametagEdit").getMethod("getApi").invoke(null);
        Class<?> apiClass = Class.forName("com.nametagedit.plugin.api.INametagApi");
        this.getNametagMethod = apiClass.getMethod("getNametag", Player.class);
        this.reloadNametagMethod = apiClass.getMethod("reloadNametag", Player.class);
        this.setPrefixMethod = apiClass.getMethod("setPrefix", Player.class, String.class);
        this.setSuffixMethod = apiClass.getMethod("setSuffix", Player.class, String.class);

        Class<? extends Event> firstLoadedEventClass = Class
                .forName("com.nametagedit.plugin.api.events.NametagFirstLoadedEvent")
                .asSubclass(Event.class);
        Method getPlayerMethod = firstLoadedEventClass.getMethod("getPlayer");
        Bukkit.getPluginManager().registerEvent(firstLoadedEventClass, this, EventPriority.NORMAL, (listener, event) -> {
            if (firstLoadedEventClass.isInstance(event)) {
                try {
                    onNametagFirstLoaded((Player) getPlayerMethod.invoke(event));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot handle NametagEdit event", e);
                }
            }
        }, Chatty.instance());
    }

    public void setPrefix(Player player, String prefix) {
        if (prefix == null) {
            String suffix = getNametagPart(player, "getSuffix");
            invoke(reloadNametagMethod, player);

            Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () ->
                    invoke(setSuffixMethod, player, suffix), 10L);
        } else invoke(setPrefixMethod, player, prefix);
    }

    public void setSuffix(Player player, String suffix) {
        if (suffix == null) {
            String prefix = getNametagPart(player, "getPrefix");
            invoke(reloadNametagMethod, player);

            Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () ->
                    invoke(setPrefixMethod, player, prefix), 10L);
        } else invoke(setSuffixMethod, player, suffix);
    }

    private void onNametagFirstLoaded(Player player) {
        if (configuration.getNode("miscellaneous.commands.prefix.enable").getAsBoolean(false)
                && configuration.getNode("miscellaneous.commands.prefix.auto-nte").getAsBoolean(false)) {
            jsonStorage.getProperty(player, "prefix").ifPresent(prefix -> setPrefix(player, prefix.getAsString()));
        }

        if (configuration.getNode("miscellaneous.commands.suffix.enable").getAsBoolean(false)
                && configuration.getNode("miscellaneous.commands.suffix.auto-nte").getAsBoolean(false)) {
            jsonStorage.getProperty(player, "suffix").ifPresent(suffix -> setSuffix(player, suffix.getAsString()));
        }
    }

    private String getNametagPart(Player player, String getterName) {
        Object nametag = invoke(getNametagMethod, player);
        try {
            return (String) nametag.getClass().getMethod(getterName).invoke(nametag);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read NametagEdit nametag", e);
        }
    }

    private Object invoke(Method method, Object... args) {
        try {
            return method.invoke(api, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot call NametagEdit API", e);
        }
    }

}

package ru.brikster.chatty.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public class ChattyPlaceholderApiExpansion extends PlaceholderExpansion implements Relational {

    @Inject private Plugin plugin;

    @Override
    public String getIdentifier() {
        return "chatty";
    }

    @Override
    public String getAuthor() {
        return "Brikster";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player one, Player two, String params) {
        // TODO finish placeholders
        String[] args = params.split(Pattern.quote("_"));
        if (args.length == 0) {
            return null;
        }
        switch (args[0]) {
            case "ignore": {
                if (args.length != 1) {
                    return null;
                }
            }
        }
        return null;
    }

}

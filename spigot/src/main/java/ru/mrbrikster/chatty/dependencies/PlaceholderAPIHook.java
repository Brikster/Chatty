package ru.mrbrikster.chatty.dependencies;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;

import java.util.List;
import java.util.Locale;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ChatManager chatManager;

    public PlaceholderAPIHook(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    public String setPlaceholders(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    public List<String> setPlaceholders(Player player, List<String> messages) {
        return PlaceholderAPI.setPlaceholders(player, messages);
    }

    @Override
    public String getIdentifier() {
        return "chatty";
    }

    @Override
    public String getAuthor() {
        return "MrBrikster";
    }

    @Override
    public String getVersion() {
        return Chatty.instance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.isEmpty()) {
            return "Chatty is awesome";
        }

        String[] split = params.split("_");
        switch (split[0].toLowerCase(Locale.ENGLISH)) {
            case "cooldown": {
                if (split.length > 1) {
                    // To support player names with underscore
                    split = params.split("_", 3);

                    Chat chat = chatManager.getChat(split[1]);
                    if (chat == null) {
                        return "-1";
                    }

                    if (split.length > 2) {
                        player = Bukkit.getPlayerExact(split[2]);
                    }

                    if (player == null) {
                        return "-1";
                    }

                    return Long.toString(Math.max(0, chat.getCooldown(player)));
                }

                return "-1";
            }

            case "current": {
                split = params.split("_", 2);

                if (split.length > 1) {
                    player = Bukkit.getPlayerExact(split[1]);
                }

                if (player == null) {
                    return "null";
                }

                Chat chat = chatManager.getCurrentChat(player);
                return chat == null ? "no" : chat.getDisplayName();
            }

            case "online": {
                if (split.length < 2) {
                    return "-1";
                }

                Chat chat = chatManager.getChat(split[1]);
                if (chat == null) {
                    return "-1";
                }

                int i = 0;
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (chat.isWriteAllowed(onlinePlayer)) i++;
                }

                return Integer.toString(i);
            }

            default:
                return null;
        }
    }

}

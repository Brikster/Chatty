package ru.mrbrikster.chatty.dependencies;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.reflection.Reflection;

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
    public boolean persist() {
        return true;
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
    public String onPlaceholderRequest(Player player, String params) {
        String[] split = params.split("_");
        if(split.length == 0) {
            return "Chatty!";
        }
        switch (split[0].toLowerCase(Locale.ENGLISH)) {
            case "cooldown":
                if(player == null) {
                    return "-1";
                }
                if(split.length > 1) {
                    Chat chat = chatManager.getChat(split[1]);
                    if(chat == null) {
                        return "-1";
                    }
                    return Long.toString(Math.max(0, chat.getCooldown(player)));
                } else {
                    return Long.toString(Math.max(0, chatManager.getChats().get(0).getCooldown(player)));
                }

            case "current":
                if(player == null) {
                    return "null";
                } else {
                    Chat chat = chatManager.getCurrentChat(player);
                    return chat == null ? "no" : chat.getName();
                }

            case "onlinein":
                Chat chat = split.length > 1 ? chatManager.getChat(split[1]) : chatManager.getChats().get(0);
                if(chat == null) {
                    return "-1";
                }
                int i = 0;
                for(Player pl : Reflection.getOnlinePlayers()) {
                    if(chat.isWriteAllowed(pl)) i++;
                }
                return Integer.toString(i);

            default:
                return null;
        }
    }
}

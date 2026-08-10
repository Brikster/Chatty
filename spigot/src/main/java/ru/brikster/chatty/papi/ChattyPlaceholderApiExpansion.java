package ru.brikster.chatty.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.LastMessageState;
import ru.brikster.chatty.chat.LastMessageState.LastMessage;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelectionState;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.brikster.chatty.repository.player.PlayerDataRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;

@Singleton
public class ChattyPlaceholderApiExpansion extends PlaceholderExpansion implements Relational {

    @Inject private Plugin plugin;
    @Inject private PlayerDataRepository playerDataRepository;
    @Inject private PrefixProvider prefixProvider;
    @Inject private ChatRegistry chatRegistry;
    @Inject private ChatSelectionState selectionState;
    @Inject private LastMessageState lastMessageState;

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
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return null;
        }

        switch (params.toLowerCase()) {
            case "prefix":
                return orEmpty(prefixProvider.getPrefix(player));
            case "suffix":
                return orEmpty(prefixProvider.getSuffix(player));
            default:
                break;
        }

        Player online = player.getPlayer();
        if (online == null) {
            return null;
        }

        switch (params.toLowerCase()) {
            case "chat":
                return currentChat(online) == null ? "" : currentChat(online).getId();
            case "chat_displayname":
                return currentChat(online) == null ? "" : currentChat(online).getDisplayName();
            case "chat_range":
                return currentChat(online) == null ? "" : String.valueOf(currentChat(online).getRange());
            case "spy":
                return Boolean.toString(playerDataRepository.isEnableSpy(online.getUniqueId()));
            case "player_message":
                return lastMessage(online) == null ? "" : lastMessage(online).getMessage();
            case "targetname":
                return lastMessage(online) == null ? online.getName() : lastMessage(online).getTargetName();
            default:
                break;
        }

        String lower = params.toLowerCase();
        if (lower.startsWith("chat_range_")) {
            Chat chat = chatRegistry.getChats().get(params.substring("chat_range_".length()));
            return chat == null ? "" : String.valueOf(chat.getRange());
        }
        if (lower.startsWith("chat_displayname_")) {
            Chat chat = chatRegistry.getChats().get(params.substring("chat_displayname_".length()));
            return chat == null ? "" : chat.getDisplayName();
        }

        return null;
    }

    @Override
    public String onPlaceholderRequest(Player one, Player two, String params) {
        if (one == null || two == null) {
            return null;
        }
        if (params.equalsIgnoreCase("ignore")) {
            return Boolean.toString(playerDataRepository
                    .isIgnoredPlayer(one.getUniqueId(), two.getUniqueId()));
        }
        return null;
    }

    private LastMessage lastMessage(Player player) {
        return lastMessageState.get(player.getUniqueId());
    }

    private Chat currentChat(Player player) {
        String switched = selectionState.getSwitchedChat(player.getUniqueId());
        if (switched != null) {
            Chat chat = chatRegistry.getChats().get(switched);
            if (chat != null && mayWrite(chat, player)) {
                return chat;
            }
        }
        return chatRegistry.getChats().values().stream()
                .filter(chat -> chat.getSymbol().isEmpty())
                .filter(chat -> mayWrite(chat, player))
                .min(Comparator.comparing(Chat::getId))
                .orElse(null);
    }

    private static boolean mayWrite(Chat chat, Player player) {
        return !chat.isPermissionRequired()
                || chat.hasSymbolWritePermission(player)
                || chat.hasCommandWritePermission(player);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

}

package ru.mrbrikster.chatty.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.Chat;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.Utils;

public abstract class EventManager implements Listener {

    private final Main main;

    public EventManager(Main main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        String message = playerChatEvent.getMessage();

        Chat chat = null;
        boolean usingSymbol = true;
        for (Chat chatMode : main.getConfiguration().getChats()) {
            if (message.startsWith(chatMode.getSymbol()) &&
                    player.hasPermission("chatty.chat." + chatMode.getName())
                    && chatMode.isEnable()) {
                chat = chatMode;
            }
        }

        if (chat == null) {
            for (Chat chatMode : main.getConfiguration().getChats()) {
                if (player.hasPermission("chatty.chat." + chatMode.getName())
                        && chatMode.isEnable()) {
                    chat = chatMode;
                }
            }

            usingSymbol = false;
        }

        if (chat == null) {
            playerChatEvent.setCancelled(true);
            player.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-chat-mode",
                    ChatColor.RED + "Applicable chat-mode not found. You can''t send the message"));
            return;
        }

        if (usingSymbol)
            message = message.substring(chat.getSymbol().length(), message.length());

        String format = chat.getFormat();
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");
        format = format.replace("{prefix}", main.getViewManager().getPrefix(player));
        format = format.replace("{suffix}", main.getViewManager().getSuffix(player));

        playerChatEvent.setFormat(Utils.colorize(format));
        playerChatEvent.setMessage(message);

        if (chat.getRange() != -1) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().addAll(Utils.getLocalRecipients(player, chat.getRange()));
        }

        main.getLogManager().write(player, message);
    }

}

package ru.mrbrikster.chatty.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
                    (!chatMode.isPermission()
                            || player.hasPermission("chatty.chat." + chatMode.getName())
                            || player.hasPermission("chatty.chat." + chatMode.getName() + ".send"))
                    && chatMode.isEnable()) {
                chat = chatMode;
            }
        }

        if (chat == null) {
            for (Chat chatMode : main.getConfiguration().getChats()) {
                if ((!chatMode.isPermission()
                        || player.hasPermission("chatty.chat." + chatMode.getName())
                        || player.hasPermission("chatty.chat." + chatMode.getName() + ".send"))
                        && chatMode.isEnable()) {
                    chat = chatMode;
                }
            }

            usingSymbol = false;
        }

        if (chat == null) {
            playerChatEvent.setCancelled(true);
            player.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-chat-mode",
                    ChatColor.RED + "Applicable chat-mode not found. You can't send the message."));
            return;
        }

        if (usingSymbol)
            message = message.substring(chat.getSymbol().length(), message.length());

        String format = chat.getFormat();
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");
        format = format.replace("{prefix}", main.getViewManager().getPrefix(player));
        format = format.replace("{suffix}", main.getViewManager().getSuffix(player));

        message = Utils.stylish(player, message, chat.getName());

        if (ChatColor.stripColor(message).isEmpty()) {
            playerChatEvent.setCancelled(true);
            return;
        }

        boolean cooldownPermission = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown") ||
                player.hasPermission("chatty.cooldown." + chat.getName());
        long cooldown = cooldownPermission ? -1 : chat.getCooldown(player);

        if (cooldown != -1) {
            player.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-chat-mode",
                    ChatColor.RED + "Wait for {cooldown} seconds, before send message in this chat again."
                    .replace("{cooldown}", String.valueOf(cooldown))));
            playerChatEvent.setCancelled(true);
            return;
        }

        playerChatEvent.setFormat(Utils.colorize(format));
        playerChatEvent.setMessage(message);

        if (chat.getRange() > -1) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().addAll(Utils.getLocalRecipients(player, chat.getRange(), chat));

            if (playerChatEvent.getRecipients().size() <= 1) {
                String noRecipients = main.getConfiguration().getMessages().getOrDefault("no-recipients", null);

                if (noRecipients != null)
                    Bukkit.getScheduler().runTaskLater(main, () -> player.sendMessage(noRecipients), 5L);
            }

            for (Player spy : Bukkit.getOnlinePlayers()) {
                if (spy.hasPermission("chatty.spy") &&
                        !main.getCommandManager().getSpyDisabledPlayers().contains(spy) &&
                        !playerChatEvent.getRecipients().contains(spy))
                    spy.sendMessage(Utils.colorize(main.getConfiguration().getSpyFormat()
                            .replace("{format}", String.format(format, player.getName(), playerChatEvent.getMessage()))));
            }
        }


        if (!cooldownPermission) chat.setCooldown(main, player);

        if (main.getConfiguration().isAntiAdsEnabled() && !player.hasPermission("chatty.ads.bypass")
                && (Utils.containsIP(main, message) || Utils.containsDomain(main, message))) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().add(player);

            main.getLogManager().write(player, message, true);
        } else main.getLogManager().write(player, message, false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        main.getCommandManager().getSpyDisabledPlayers()
                .remove(playerQuitEvent.getPlayer());
    }

}

package ru.mrbrikster.chatty.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.mrbrikster.chatty.Chat;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.Utils;
import ru.mrbrikster.chatty.commands.CommandGroup;

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

        // Cancel empty message
        if (ChatColor.stripColor(message).isEmpty()) {
            playerChatEvent.setCancelled(true);
            return;
        }

        boolean hasCooldown = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown") ||
                player.hasPermission("chatty.cooldown." + chat.getName());
        long cooldown = hasCooldown ? -1 : chat.getCooldown(player);

        // Check cooldown
        if (cooldown != -1) {
            player.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-chat-mode",
                    ChatColor.RED + "Wait for {cooldown} seconds, before send message in this chat again."
                    .replace("{cooldown}", String.valueOf(cooldown))));
            playerChatEvent.setCancelled(true);
            return;
        }

        if (chat.getMoney() > 0 &&
                !main.getEconomyManager()
                        .withdraw(player, chat.getMoney())) {
            player.sendMessage(main.getConfiguration().getMessages().getOrDefault("chat-mode-requires",
                    ChatColor.RED + "You need {money} money to send message in this chat."
                            .replace("{money}", String.valueOf(chat.getMoney()))));
            playerChatEvent.setCancelled(true);
            return;
        }

        playerChatEvent.setFormat(Utils.colorize(format));
        playerChatEvent.setMessage(message);

        // Add new recipients
        playerChatEvent.getRecipients().clear();
        playerChatEvent.getRecipients().addAll(Utils.getRecipients(player, chat.getRange(), chat));

        // Check for "no-recipients"
        if (playerChatEvent.getRecipients().size() <= 1) {
            String noRecipients = main.getConfiguration().getMessages().getOrDefault("no-recipients", null);

            if (noRecipients != null)
                Bukkit.getScheduler().runTaskLater(main, () -> player.sendMessage(noRecipients), 5L);
        }

        if (!hasCooldown) chat.setCooldown(main, player);

        if (main.getConfiguration().isAntiAdsEnabled() && !player.hasPermission("chatty.ads.bypass")
                && (Utils.containsIP(main, message) || Utils.containsDomain(main, message))) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().add(player);

            main.getLogManager().write(player, message, true);

            String adsFound = main.getConfiguration().getMessages().getOrDefault("ads-found", null);

            if (adsFound != null)
                Bukkit.getScheduler().runTaskLater(main, () -> player.sendMessage(adsFound), 5L);
        } else main.getLogManager().write(player, message, false);

        // Send to spy-players
        for (Player spy : Bukkit.getOnlinePlayers()) {
            if ((spy.hasPermission("chatty.spy") || spy.hasPermission("chatty.spy." + chat.getName())) &&
                    !main.getCommandManager().getSpyDisabledPlayers().contains(spy) &&
                    !playerChatEvent.getRecipients().contains(spy))
                spy.sendMessage(Utils.colorize(main.getConfiguration().getSpyFormat()
                        .replace("{format}", String.format(format, player.getName(), playerChatEvent.getMessage()))));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        main.getCommandManager().getSpyDisabledPlayers()
                .remove(playerQuitEvent.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandGroupListener(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        if (!playerCommandPreprocessEvent.getMessage().contains("/"))
            return;

        String message = playerCommandPreprocessEvent.getMessage().replaceAll("/", "");

        boolean block = false;
        String response = null;
        long cooldown = -1;
        CommandGroup cooldownGroup = null;

        commandGroups: for (CommandGroup commandGroup : main.getConfiguration().getCommandGroups()) {
            if (playerCommandPreprocessEvent.getPlayer().hasPermission("chatty.command." + commandGroup.getName()))
                continue;

            for (CommandGroup.Trigger trigger : commandGroup.getTriggers()) {
                boolean triggered = false;
                switch (trigger.getType()) {
                    case STARTS:
                        if (message.split(" ")[0].toLowerCase().startsWith(trigger.getData().toLowerCase())) {
                            triggered = true;
                        }
                        break;
                    case COMMAND:
                        if (message.split(" ")[0].equalsIgnoreCase(trigger.getData())) {
                            triggered = true;
                        }
                        break;
                    case CONTAINS:
                        if (message.toLowerCase().contains(trigger.getData().toLowerCase())) {
                            triggered = true;
                        }
                        break;
                }

                if (triggered) {
                    block = commandGroup.isBlock();
                    response = commandGroup.getMessage();
                    if (block) break commandGroups;

                    if (cooldown < commandGroup.getCooldown()) {
                        cooldown = commandGroup.getCooldown();
                        cooldownGroup = commandGroup;
                    }
                    response = commandGroup.getMessage();
                    continue commandGroups;
                }
            }
        }

        if (block) {
            playerCommandPreprocessEvent.setCancelled(true);

            if (response != null)
                playerCommandPreprocessEvent.getPlayer().sendMessage(Utils.colorize(response));

            return;
        }

        if (cooldown > 0) {
            long playerCooldown;
            if ((playerCooldown = cooldownGroup.getCooldown(playerCommandPreprocessEvent.getPlayer())) > 0) {
                playerCommandPreprocessEvent.setCancelled(true);
                if (response != null) playerCommandPreprocessEvent.getPlayer().sendMessage(
                        Utils.colorize(response.replace("{cooldown}", String.valueOf(playerCooldown))));
            } else {
                cooldownGroup.setCooldown(main, playerCommandPreprocessEvent.getPlayer());
            }
        }
    }

}

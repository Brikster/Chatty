package ru.mrbrikster.chatty.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.Utils;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyPool;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.dependencies.VaultHook;

import java.util.function.Function;

public abstract class ChatListener implements Listener {

    private static final String MESSAGES_NODE = "messages";
    private static final Function<String, String> COLORIZE = (string) -> string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    private final DependencyPool dependencyPool;
    private final ChatManager chatManager;
    private final Configuration configuration;

    @SuppressWarnings("all")
    public ChatListener(Configuration configuration,
                        ChatManager chatManager,
                        DependencyPool dependencyPool) {
        this.configuration = configuration;
        this.chatManager = chatManager;
        this.dependencyPool = dependencyPool;
    }

    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        String message = playerChatEvent.getMessage();

        Chat chat = null;


        boolean usingSymbol = true;
        for (Chat chatMode : chatManager.getChats()) {
            if (message.startsWith(chatMode.getSymbol()) &&
                    (!chatMode.isPermission()
                            || player.hasPermission(String.format("chatty.chat.%s", chatMode))
                            || player.hasPermission(String.format("chatty.chat.%s.write", chatMode)))
                    && chatMode.isEnable()) {
                chat = chatMode;
            }
        }

        if (chat == null) {
            for (Chat chatMode : chatManager.getChats()) {
                if ((!chatMode.isPermission()
                        || player.hasPermission(String.format("chatty.chat.%s", chatMode.getName()))
                        || player.hasPermission(String.format("chatty.chat.%s.write", chatMode)))
                        && chatMode.isEnable()) {
                    chat = chatMode;
                }
            }

            usingSymbol = false;
        }

        if (chat == null) {
            playerChatEvent.setCancelled(true);
            player.sendMessage(COLORIZE.apply(configuration.getNode(MESSAGES_NODE).getNode("chat-not-found").getAsString(
                    ChatColor.RED + "Applicable chat not found. You can't send the message.")));
            return;
        }

        if (usingSymbol)
            message = message.substring(chat.getSymbol().length(), message.length());

        String prefix = "", suffix = "";

        if (dependencyPool.getDependency(VaultHook.class) != null) {
            VaultHook vaultHook = dependencyPool.getDependency(VaultHook.class);
            prefix = vaultHook.getPrefix(player);
            suffix = vaultHook.getSuffix(player);

            if (prefix == null) prefix = "";
            if (suffix == null) suffix = "";
        }

        String format = chat.getFormat();
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");
        format = format.replace("{prefix}", prefix);
        format = format.replace("{suffix}", suffix);

        message = Utils.stylish(player, message, chat.getName());

        // Cancel empty message
        if (ChatColor.stripColor(message).isEmpty()) {
            playerChatEvent.setCancelled(true);
            return;
        }

        boolean hasCooldown = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown") ||
                player.hasPermission(String.format("chatty.cooldown.%s", chat.getName()));
        long cooldown = hasCooldown ? -1 : chat.getCooldown(player);

        // Check cooldown
        if (cooldown != -1) {
            player.sendMessage(COLORIZE.apply(configuration.getNode(MESSAGES_NODE).getNode("cooldown").getAsString(
                    ChatColor.RED + "Wait for {cooldown} seconds, before send message in this chat again.")
                    .replace("{cooldown}", String.valueOf(cooldown))));
            playerChatEvent.setCancelled(true);
            return;
        }

        if (chat.getMoney() > 0 && dependencyPool.getDependency(VaultHook.class) != null) {
            VaultHook vaultHook = dependencyPool.getDependency(VaultHook.class);

            if (!vaultHook.withdrawMoney(player, chat.getMoney())) {
                player.sendMessage(
                        COLORIZE.apply(configuration.getNode(MESSAGES_NODE).getNode("not-enough-money").getAsString(
                        ChatColor.RED + "You need {money} money to send message in this chat."
                                .replace("{money}", String.valueOf(chat.getMoney())))));
                playerChatEvent.setCancelled(true);
                return;
            }
        }

        playerChatEvent.setFormat(Utils.colorize(format));
        playerChatEvent.setMessage(message);

        if (dependencyPool.getDependency(PlaceholderAPIHook.class) != null) {
            playerChatEvent.setFormat(dependencyPool.getDependency(PlaceholderAPIHook.class)
                    .setPlaceholders(player, playerChatEvent.getFormat()));
        }

        // Add new recipients
        playerChatEvent.getRecipients().clear();
        playerChatEvent.getRecipients().addAll(chat.getRecipients(player));

        // Check for "no-recipients"
        if (playerChatEvent.getRecipients().size() <= 1) {
            String noRecipients = COLORIZE.apply(configuration.getNode(MESSAGES_NODE).getNode("no-recipients").getAsString(null));

            if (noRecipients != null)
                Bukkit.getScheduler().runTaskLater(Chatty.instance(), () -> player.sendMessage(noRecipients), 5L);
        }

        if (!hasCooldown) chat.setCooldown(player);

        /*
        if (chatty.getConfiguration().isAntiAdsEnabled() && !player.hasPermission("chatty.ads.bypass")
                && (Utils.containsIP(chatty, message) || Utils.containsDomain(chatty, message))) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().add(player);

            chatty.getLogManager().write(player, message, true);

            String adsFound = COLORIZE.apply(configuration.getNode(MESSAGES_NODE).getNode("advertisement-found").getAsString(null));

            if (adsFound != null)
                Bukkit.getScheduler().runTaskLater(chatty, () -> player.sendMessage(adsFound), 5L);
        } else chatty.getLogManager().write(player, message, false);

        if (chatty.getConfiguration().isSpyEnabled()) {
            playerChatEvent.setFormat(chat.getName() + "|" + playerChatEvent.getFormat());
        }
        */
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpy(AsyncPlayerChatEvent playerChatEvent) {
        /*
        if (!chatty.getConfiguration().isSpyEnabled())
            return;

        String[] formatSplit = playerChatEvent.getFormat().split("\\|", 2);
        for (Player spy : Utils.getOnlinePlayers()) {
            if ((spy.hasPermission("chatty.spy") || spy.hasPermission("chatty.spy." + formatSplit[0])) &&
                    !chatty.getCommandManager().getSpyDisabledPlayers().contains(spy) &&
                    !playerChatEvent.getRecipients().contains(spy))
                spy.sendMessage(Utils.colorize(chatty.getConfiguration().getSpyFormat()
                        .replace("{format}", String.format(formatSplit[1], playerChatEvent.getPlayer().getName(), playerChatEvent.getMessage()))));
        }

        playerChatEvent.setFormat(formatSplit[1]);
        */
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        //chatty.getCommandManager().getSpyDisabledPlayers().remove(playerQuitEvent.getPlayer());
    }

}

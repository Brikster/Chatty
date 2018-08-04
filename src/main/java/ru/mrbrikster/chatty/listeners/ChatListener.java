package ru.mrbrikster.chatty.listeners;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.fanciful.FancyMessage;
import ru.mrbrikster.chatty.moderation.CapsModerationMethod;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.reflection.Reflection;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class ChatListener implements Listener {

    private static final Function<String, String> COLORIZE
            = (string) -> string == null ? null : ChatColor.translateAlternateColorCodes('&', string);

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)&([0-9A-F])");
    private static final Pattern MAGIC_PATTERN = Pattern.compile("(?i)&([K])");
    private static final Pattern BOLD_PATTERN = Pattern.compile("(?i)&([L])");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("(?i)&([M])");
    private static final Pattern UNDERLINE_PATTENT = Pattern.compile("(?i)&([N])");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?i)&([O])");
    private static final Pattern RESET_PATTERN = Pattern.compile("(?i)&([R])");
    private static final String PERMISSION_PREFIX = "chatty.style.";

    private static final Map<String, Pattern> PATTERNS = ImmutableMap.<String, Pattern>builder()
            .put(PERMISSION_PREFIX + "colors", COLOR_PATTERN)
            .put(PERMISSION_PREFIX + "magic", MAGIC_PATTERN)
            .put(PERMISSION_PREFIX + "bold", BOLD_PATTERN)
            .put(PERMISSION_PREFIX + "strikethrough", STRIKETHROUGH_PATTERN)
            .put(PERMISSION_PREFIX + "underline", UNDERLINE_PATTENT)
            .put(PERMISSION_PREFIX + "italic", ITALIC_PATTERN)
            .put(PERMISSION_PREFIX + "reset", RESET_PATTERN).build();

    private final DependencyManager dependencyManager;
    private final ChatManager chatManager;
    private final Configuration configuration;
    private final ModerationManager moderationManager;

    @SuppressWarnings("all")
    public ChatListener(Configuration configuration,
                        ChatManager chatManager,
                        DependencyManager dependencyManager,
                        ModerationManager moderationManager) {
        this.configuration = configuration;
        this.chatManager = chatManager;
        this.dependencyManager = dependencyManager;
        this.moderationManager = moderationManager;
    }

    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        String message = playerChatEvent.getMessage();

        Chat chat = null;

        boolean usingSymbol = true;
        for (Chat chatMode : chatManager.getChats()) {
            if (message.startsWith(chatMode.getSymbol()) &&
                    (!chatMode.isPermission()
                            || player.hasPermission(String.format("chatty.chat.%s", chatMode.getName()))
                            || player.hasPermission(String.format("chatty.chat.%s.write", chatMode.getName())))
                    && chatMode.isEnable()) {
                chat = chatMode;
            }
        }

        if (chat == null) {
            for (Chat chatMode : chatManager.getChats()) {
                if ((!chatMode.isPermission()
                        || player.hasPermission(String.format("chatty.chat.%s", chatMode.getName()))
                        || player.hasPermission(String.format("chatty.chat.%s.write", chatMode.getName())))
                        && chatMode.isEnable()) {
                    chat = chatMode;
                }
            }

            usingSymbol = false;
        }

        if (chat == null) {
            playerChatEvent.setCancelled(true);
            player.sendMessage(Configuration.getMessages().get("chat-not-found"));
            return;
        }

        if (usingSymbol)
            message = message.substring(chat.getSymbol().length(), message.length());

        String prefix = "", suffix = "";

        if (dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();
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

        message = stylish(player, message, chat.getName());

        if (ChatColor.stripColor(message).isEmpty()) {
            playerChatEvent.setCancelled(true);
            return;
        }

        boolean hasCooldown = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown") ||
                player.hasPermission(String.format("chatty.cooldown.%s", chat.getName()));
        long cooldown = hasCooldown ? -1 : chat.getCooldown(player);

        if (cooldown != -1) {
            player.sendMessage(Configuration.getMessages().get("cooldown")
                    .replace("{cooldown}", String.valueOf(cooldown)));
            playerChatEvent.setCancelled(true);
            return;
        }

        if (chat.getMoney() > 0 && dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();

            if (!vaultHook.withdrawMoney(player, chat.getMoney())) {
                player.sendMessage(Configuration.getMessages().get("not-enough-money")
                                .replace("{money}", String.valueOf(chat.getMoney())));
                playerChatEvent.setCancelled(true);
                return;
            }
        }

        playerChatEvent.setFormat(COLORIZE.apply(format));
        playerChatEvent.setMessage(message);

        if (dependencyManager.getPlaceholderApi() != null) {
            playerChatEvent.setFormat(dependencyManager.getPlaceholderApi()
                    .setPlaceholders(player, playerChatEvent.getFormat()));
        }

        playerChatEvent.getRecipients().clear();
        playerChatEvent.getRecipients().addAll(chat.getRecipients(player));

        if (playerChatEvent.getRecipients().size() <= 1) {
            String noRecipients = Configuration.getMessages()
                    .get("no-recipients", null);

            if (noRecipients != null)
                Bukkit.getScheduler().runTaskLater(Chatty.instance(), () -> player.sendMessage(noRecipients), 5L);
        }

        if (!hasCooldown) chat.setCooldown(player);

        CapsModerationMethod capsModerationMethod;
        if (moderationManager.isCapsModerationEnabled() &&
                !player.hasPermission("chatty.moderation.caps")
                && (capsModerationMethod = moderationManager.getCapsMethod(message)).isBlocked()) {
            if (capsModerationMethod.isUseBlock()) {
                playerChatEvent.getRecipients().clear();
                playerChatEvent.getRecipients().add(player);

                chatManager.getLogger().write(player, message, "[CAPS] ");
            } else {
                playerChatEvent.setMessage(capsModerationMethod.getEditedMessage());
                chatManager.getLogger().write(player, message, "");
            }

            String capsFound = Configuration.getMessages().get("caps-found", null);

            if (capsFound != null)
                Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                        () -> player.sendMessage(capsFound), 5L);
        } else if (moderationManager.isAdvertisementModerationEnabled() &&
                !player.hasPermission("chatty.moderation.advertisement")
                && moderationManager.getAdvertisementMethod(message).isBlocked()) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().add(player);

            chatManager.getLogger().write(player, message, "[ADS] ");

            String adsFound = Configuration.getMessages().get("advertisement-found", null);

            if (adsFound != null)
                Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                        () -> player.sendMessage(adsFound), 5L);
        } else chatManager.getLogger().write(player, message, "");

        if (configuration.getNode("general.spy.enable").getAsBoolean(false)) {
            playerChatEvent.setFormat(chat.getName() + "|" + playerChatEvent.getFormat());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJsonMessage(AsyncPlayerChatEvent playerChatEvent) {
        // Spy-mode
        if (configuration.getNode("general.spy.enable").getAsBoolean(false)) {
            String[] formatSplit = playerChatEvent.getFormat().split("\\|", 2);
            Reflection.getOnlinePlayers().stream().filter(spy -> (spy.hasPermission("chatty.spy") || spy.hasPermission("chatty.spy." + formatSplit[0])) &&
                    !chatManager.getSpyDisabled().contains(spy) &&
                    !playerChatEvent.getRecipients().contains(spy)).forEach(spy -> spy.sendMessage(COLORIZE.apply(configuration.getNode("general.spy.format")
                    .getAsString("&6[Spy] &r{format}").replace("{format}",
                            String.format(formatSplit[1], playerChatEvent.getPlayer().getName(), playerChatEvent.getMessage())))));

            playerChatEvent.setFormat(formatSplit[1]);
        }

        // JSON format
        if (!configuration.getNode("json.enable").getAsBoolean(false))
            return;

        Player player = playerChatEvent.getPlayer();
        String format = String.format(playerChatEvent.getFormat(),
                "{player_name}", playerChatEvent.getMessage());

        String[] formatSplit = format.split(Pattern.quote("{player_name}"), 2);

        List<String> tooltip = dependencyManager.getPlaceholderApi().setPlaceholders(player,
                configuration.getNode("json.tooltip").getAsStringList())
                .stream().map(line -> line.replace("{player}", player.getName())).collect(Collectors.toList());

        String command = configuration.getNode("json.command").getAsString(null);
        String suggestCommand = configuration.getNode("json.suggest_command").getAsString(null);

        if (command != null) command = dependencyManager.getPlaceholderApi().setPlaceholders(player, command).replace("{player}", player.getName());
        if (suggestCommand != null) suggestCommand = suggestCommand.replace("{player}", player.getName());

        FancyMessage fancyMessage = new FancyMessage();
        if (formatSplit.length == 2) {
            fancyMessage
                    .text(formatSplit[0]);

            fancyMessage.then(
                    ChatColor.getLastColors(formatSplit[0]) + player.getName());

            if (!tooltip.isEmpty())
                fancyMessage.tooltip(tooltip);
            if (command != null)
                fancyMessage.command(command);
            if (suggestCommand != null)
                fancyMessage.suggest(suggestCommand);

            fancyMessage.then(formatSplit[1]);
        } else if (formatSplit.length == 1) {
            fancyMessage
                    .text(player.getName());

            if (!tooltip.isEmpty())
                fancyMessage.tooltip(tooltip);
            if (command != null)
                fancyMessage.command(command);
            if (suggestCommand != null)
                fancyMessage.suggest(suggestCommand);

            fancyMessage.then(formatSplit[0]);
        } else return;

        playerChatEvent.setCancelled(true);
        playerChatEvent.getRecipients().forEach(fancyMessage::send);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        chatManager.getSpyDisabled()
                .remove(playerQuitEvent.getPlayer());
    }

    private String stylish(Player player, String message, String chat) {
        for (Map.Entry<String, Pattern> entry : PATTERNS.entrySet()) {
            if (player.hasPermission(entry.getKey()) || player.hasPermission(entry.getKey() + "." + chat)) {
                message = entry.getValue().matcher(message).replaceAll("\u00A7$1");
            }
        }

        return message;
    }

}

package ru.mrbrikster.chatty.listeners;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.json.JSONMessagePart;
import ru.mrbrikster.chatty.json.LegacyMessagePart;
import ru.mrbrikster.chatty.moderation.CapsModerationMethod;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;
import ru.mrbrikster.chatty.reflection.Reflection;

import java.util.IdentityHashMap;
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
    private final TemporaryStorage temporaryStorage;
    private final Configuration configuration;
    private final ModerationManager moderationManager;
    private final PermanentStorage permanentStorage;
    private IdentityHashMap<Player, Chat> pendingPlayers;
    private IdentityHashMap<Player, List<String>> pendingSwears;

    @SuppressWarnings("all")
    public ChatListener(Configuration configuration,
                        ChatManager chatManager,
                        TemporaryStorage temporaryStorage,
                        DependencyManager dependencyManager,
                        ModerationManager moderationManager,
                        PermanentStorage permanentStorage) {
        this.configuration = configuration;
        this.chatManager = chatManager;
        this.temporaryStorage = temporaryStorage;
        this.dependencyManager = dependencyManager;
        this.moderationManager = moderationManager;
        this.permanentStorage = permanentStorage;

        this.pendingPlayers = new IdentityHashMap<>();
        this.pendingSwears = new IdentityHashMap<>();
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

        String format = chat.getFormat();
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");
        format = format.replace("{prefix}", getPrefix(player));
        format = format.replace("{suffix}", getSuffix(player));

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
        playerChatEvent.getRecipients().addAll(chat.getRecipients(player, permanentStorage));

        if (playerChatEvent.getRecipients().size() <= 1) {
            String noRecipients = Configuration.getMessages()
                    .get("no-recipients", null);

            if (noRecipients != null)
                Bukkit.getScheduler().runTaskLater(Chatty.instance(), () -> player.sendMessage(noRecipients), 5L);
        }

        if (!hasCooldown) chat.setCooldown(player);

        CapsModerationMethod capsModerationMethod;
        SwearModerationMethod swearModerationMethod = null;
        boolean swearModerationEnabled = moderationManager.isSwearModerationEnabled();
        if (swearModerationEnabled && !player.hasPermission("chatty.moderation.swear")
                && (swearModerationMethod = moderationManager.getSwearMethod(message)).isBlocked()) {
            playerChatEvent.getRecipients().clear();
            playerChatEvent.getRecipients().add(player);
            playerChatEvent.setMessage(swearModerationMethod.getEditedMessage());

            chatManager.getLogger().write(player, message, "[SWEAR] ");

            if (configuration.getNode("json.enable").getAsBoolean(false)
                    && configuration.getNode("json.swears.enable").getAsBoolean(false)) {
                pendingSwears.put(playerChatEvent.getPlayer(), swearModerationMethod.getWords());
            }

            String swearFound = Configuration.getMessages().get("swear-found", null);

            if (swearFound != null)
                Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                        () -> player.sendMessage(swearFound), 5L);
        } else {
            if (swearModerationEnabled && swearModerationMethod != null && !player.hasPermission("chatty.moderation.swear")) {
                if (configuration.getNode("json.enable").getAsBoolean(false)
                        && configuration.getNode("json.swears.enable").getAsBoolean(false)) {
                    pendingSwears.put(playerChatEvent.getPlayer(), swearModerationMethod.getWords());
                }

                playerChatEvent.setMessage(swearModerationMethod.getEditedMessage());
            }

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
        }

        pendingPlayers.put(player, chat);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpyMessage(AsyncPlayerChatEvent playerChatEvent) {
        if (configuration.getNode("general.spy.enable").getAsBoolean(false)) {
            Chat chat = pendingPlayers.remove(playerChatEvent.getPlayer());

            if (!playerChatEvent.isCancelled()) {
                Reflection.getOnlinePlayers().stream().filter(spy -> (spy.hasPermission("chatty.spy") || spy.hasPermission("chatty.spy." + chat.getName()))
                        && !temporaryStorage.getSpyDisabled().contains(spy) &&
                        !playerChatEvent.getRecipients().contains(spy)).
                        forEach(spy ->
                                spy.sendMessage(
                                        COLORIZE.apply(configuration.getNode("general.spy.format").getAsString("&6[Spy] &r{format}")
                                                .replace("{format}", String.format(playerChatEvent.getFormat(), playerChatEvent.getPlayer().getName(), playerChatEvent.getMessage())))));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJsonMessage(AsyncPlayerChatEvent playerChatEvent) {
        if (!configuration.getNode("json.enable").getAsBoolean(false)) return;

        Player player = playerChatEvent.getPlayer();
        String format = unstylish(String.format(playerChatEvent.getFormat(),
                "{player}", "{message}"));

        PlaceholderAPIHook placeholderAPI = dependencyManager.getPlaceholderApi();
        List<String> tooltip = configuration.getNode("json.tooltip").getAsStringList()
                .stream().map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{player}", player.getName())))
                .collect(Collectors.toList());

        if (placeholderAPI != null)
            tooltip = placeholderAPI.setPlaceholders(player, tooltip);

        String command = configuration.getNode("json.command").getAsString(null);
        String suggestCommand = configuration.getNode("json.suggest_command").getAsString(null);
        String link = configuration.getNode("json.link").getAsString(null);

        Function<String, String> replaceVariables = string -> {
            if (string == null) return null;

            string = string.replace("{player}", player.getName());

            if (placeholderAPI != null)
                string = placeholderAPI.setPlaceholders(player, string);

            return string;
        };

        FormattedMessage formattedMessage = new FormattedMessage(format);
        formattedMessage.replace("{player}",
                new JSONMessagePart(player.getName())
                    .command(replaceVariables.apply(command))
                    .suggest(replaceVariables.apply(suggestCommand))
                    .link(replaceVariables.apply(link))
                    .tooltip(tooltip));

        configuration.getNode("json.replacements").getChildNodes().forEach(replacement -> {
            String replacementName = replacement.getName();
            String text = replacement.getNode("text").getAsString(replacementName);
            List<String> replacementTooltip = replacement.getNode("tooltip").getAsStringList();

            replacementTooltip = replacementTooltip.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{player}", player.getName())))
                    .collect(Collectors.toList());

            if (placeholderAPI != null)
                replacementTooltip = placeholderAPI.setPlaceholders(player, replacementTooltip);

            String replacementCommand = replacement.getNode("command").getAsString(null);
            String replacementSuggestCommand = replacement.getNode("suggest_command").getAsString(null);
            String replacementLink = replacement.getNode("link").getAsString(null);

            formattedMessage.replace(replacementName, new JSONMessagePart(replaceVariables.apply(text))
                    .command(replaceVariables.apply(replacementCommand))
                    .suggest(replaceVariables.apply(replacementSuggestCommand))
                    .link(replaceVariables.apply(replacementLink))
                    .tooltip(replacementTooltip));
        });

        formattedMessage.replace("{message}", new LegacyMessagePart(playerChatEvent.getMessage()));

        if (configuration.getNode("json.swears.enable").getAsBoolean(false)) {
            String replacement = configuration.getNode("moderation.swear.replacement").getAsString("<swear>");
            List<String> swears = pendingSwears.remove(playerChatEvent.getPlayer());

            if (player.hasPermission("chatty.swears.see")) {
                List<String> swearTooltip = configuration.getNode("json.swears.tooltip").getAsStringList()
                        .stream().map(tooltipLine -> ChatColor.translateAlternateColorCodes('&', tooltipLine)).collect(Collectors.toList());

                String suggest = configuration.getNode("json.swears.suggest_command").getAsString(null);

                if (swears != null) swears.forEach(swear -> formattedMessage.replace(replacement,
                        new JSONMessagePart(replacement)
                                .tooltip(swearTooltip.stream().map(tooltipLine -> tooltipLine.replace("{word}", swear)).collect(Collectors.toList()))
                                .suggest(suggest != null ? suggest.replace("{word}", swear) : null)));
            }
        }

        formattedMessage.send(playerChatEvent.getRecipients());

        playerChatEvent.getRecipients().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        String joinMessage = configuration
                .getNode("misc.join.message")
                .getAsString(null);

        if (joinMessage != null) {
            if (joinMessage.isEmpty() ||
                    (configuration.getNode("misc.join.permission")
                            .getAsBoolean(false)
                            && !playerJoinEvent.getPlayer().hasPermission("chatty.misc.joinmessage"))) {
                playerJoinEvent.setJoinMessage(null);
            } else playerJoinEvent.setJoinMessage(COLORIZE.apply(joinMessage
                    .replace("{prefix}", getPrefix(playerJoinEvent.getPlayer()))
                    .replace("{suffix}", getSuffix(playerJoinEvent.getPlayer()))
                    .replace("{player}", playerJoinEvent.getPlayer().getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        temporaryStorage.getSpyDisabled().remove(playerQuitEvent.getPlayer());

        String quitMessage = configuration
                .getNode("misc.quit.message")
                .getAsString(null);

        if (quitMessage != null) {
            if (quitMessage.isEmpty() ||
                    (configuration.getNode("misc.quit.permission")
                            .getAsBoolean(false)
                            && !playerQuitEvent.getPlayer().hasPermission("chatty.misc.quitmessage"))) {
                playerQuitEvent.setQuitMessage(null);
            } else playerQuitEvent.setQuitMessage(COLORIZE.apply(quitMessage
                    .replace("{prefix}", getPrefix(playerQuitEvent.getPlayer()))
                    .replace("{suffix}", getSuffix(playerQuitEvent.getPlayer()))
                    .replace("{player}", playerQuitEvent.getPlayer().getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        String deathMessage = configuration
                .getNode("misc.death.message")
                .getAsString(null);

        if (deathMessage != null) {
            if (deathMessage.isEmpty() ||
                    (configuration.getNode("misc.death.permission")
                            .getAsBoolean(false)
                            && !playerDeathEvent.getEntity().hasPermission("chatty.misc.deathmessage"))) {
                playerDeathEvent.setDeathMessage(null);
            } else playerDeathEvent.setDeathMessage(COLORIZE.apply(deathMessage
                    .replace("{prefix}", getPrefix(playerDeathEvent.getEntity()))
                    .replace("{suffix}", getSuffix(playerDeathEvent.getEntity()))
                    .replace("{player}", playerDeathEvent.getEntity().getName())));
        }
    }

    private String getPrefix(Player player) {
        String prefix = "";

        if (dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();
            prefix = vaultHook.getPrefix(player);

            if (prefix == null) prefix = "";
        }

        return prefix;
    }

    private String getSuffix(Player player) {
        String suffix = "";

        if (dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();
            suffix = vaultHook.getSuffix(player);

            if (suffix == null) suffix = "";
        }

        return suffix;
    }

    private String stylish(Player player, String message, String chat) {
        for (Map.Entry<String, Pattern> entry : PATTERNS.entrySet()) {
            if (player.hasPermission(entry.getKey()) || player.hasPermission(entry.getKey() + "." + chat)) {
                message = entry.getValue().matcher(message).replaceAll("\u00A7$1");
            }
        }

        return message;
    }

    private String unstylish(String string) {
        char[] b = string.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '\u00A7' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = '&';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }

        return new String(b);
    }

}

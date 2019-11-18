package ru.mrbrikster.chatty.chat;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.bungee.BungeeBroadcaster;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.dependencies.PrefixAndSuffixManager;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.json.JSONMessagePart;
import ru.mrbrikster.chatty.json.LegacyMessagePart;
import ru.mrbrikster.chatty.moderation.AdvertisementModerationMethod;
import ru.mrbrikster.chatty.moderation.CapsModerationMethod;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.Pair;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatListener implements Listener, EventExecutor {

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
    private final JsonStorage jsonStorage;
    private final PrefixAndSuffixManager prefixAndSuffixManager;
    
    private IdentityHashMap<Player, Pair<Chat, List<Player>>> pendingSpyMessages;
    private IdentityHashMap<Player, List<String>> pendingSwears;
    private IdentityHashMap<Player, Chat> pendingJsonMessages;

    public ChatListener(Configuration configuration,
                        ChatManager chatManager,
                        DependencyManager dependencyManager,
                        ModerationManager moderationManager,
                        JsonStorage jsonStorage) {
        this.configuration = configuration;
        this.chatManager = chatManager;
        this.dependencyManager = dependencyManager;
        this.moderationManager = moderationManager;
        this.jsonStorage = jsonStorage;

        this.pendingSpyMessages = new IdentityHashMap<>();
        this.pendingSwears = new IdentityHashMap<>();
        this.pendingJsonMessages = new IdentityHashMap<>();
        
        this.prefixAndSuffixManager = new PrefixAndSuffixManager(dependencyManager, jsonStorage);
    }

    @Override
    public void execute(Listener listener, Event event) {
        if (listener == this && event instanceof AsyncPlayerChatEvent) {
            this.onChat((AsyncPlayerChatEvent) event);
        }
    }

    private void onChat(AsyncPlayerChatEvent playerChatEvent) {
        final Player player = playerChatEvent.getPlayer();
        String message = playerChatEvent.getMessage();

        Chat chat = getChat(player, message);

        if (chat == null) {
            playerChatEvent.setCancelled(true);
            player.sendMessage(Chatty.instance().messages().get("chat-not-found"));
            return;
        }

        boolean json = configuration.getNode("json.enable").getAsBoolean(false);

        if (!chat.getSymbol().isEmpty()) {
            message = message.substring(chat.getSymbol().length());
        }

        message = stylish(player, message, chat.getName());

        if (ChatColor.stripColor(message).isEmpty()) {
            playerChatEvent.setCancelled(true);
            return;
        }

        boolean hasCooldown = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown") ||
                player.hasPermission("chatty.cooldown." + chat.getName());
        long cooldown = hasCooldown ? -1 : chat.getCooldown(player);

        if (cooldown != -1) {
            player.sendMessage(Chatty.instance().messages().get("cooldown")
                    .replace("{cooldown}", String.valueOf(cooldown)));
            playerChatEvent.setCancelled(true);
            return;
        }

        if (chat.getMoney() > 0 && dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();

            if (!vaultHook.withdrawMoney(player, chat.getMoney())) {
                player.sendMessage(Chatty.instance().messages().get("not-enough-money")
                        .replace("{money}", String.valueOf(chat.getMoney())));
                playerChatEvent.setCancelled(true);
                return;
            }
        }

        String format = chat.getFormat();
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");
        format = format.replace("{prefix}", prefixAndSuffixManager.getPrefix(player));
        format = format.replace("{suffix}", prefixAndSuffixManager.getSuffix(player));

        if (dependencyManager.getPlaceholderApi() != null) {
            format = dependencyManager.getPlaceholderApi().setPlaceholders(player, format);
        }

        format = COLORIZE.apply(format);

        playerChatEvent.setFormat(format);

        playerChatEvent.getRecipients().clear();
        playerChatEvent.getRecipients().addAll(chat.getRecipients(player, jsonStorage));

        if (playerChatEvent.getRecipients().size() <= 1) {
            String noRecipients = Chatty.instance().messages().get("no-recipients", null);

            if (noRecipients != null && chat.getRange() > -3) {
                Bukkit.getScheduler().runTaskLater(Chatty.instance(), () -> player.sendMessage(noRecipients), 5L);
            }
        }

        if (!hasCooldown) {
            chat.setCooldown(player);
        }

        boolean cancelledByModeration = false;

        StringBuilder logPrefixBuilder = new StringBuilder();
        if (moderationManager.isSwearModerationEnabled()) {
            SwearModerationMethod swearMethod = moderationManager.getSwearMethod(message);
            if (!player.hasPermission("chatty.moderation.swear")) {
                if (swearMethod.isBlocked()) {

                    message = swearMethod.getEditedMessage();
                    if (swearMethod.isUseBlock()) {
                        if (configuration.getNode("general.completely-cancel").getAsBoolean(false))
                            cancelledByModeration = true;
                        else {
                            playerChatEvent.getRecipients().clear();
                            playerChatEvent.getRecipients().add(player);
                        }

                        logPrefixBuilder.append("[SWEAR] ");
                    }

                    String swearFound = Chatty.instance().messages().get("swear-found", null);

                    if (swearFound != null)
                        Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                                () -> player.sendMessage(swearFound), 5L);
                }

                if (configuration.getNode("json.enable").getAsBoolean(false)
                        && configuration.getNode("json.swears.enable").getAsBoolean(false)) {
                    pendingSwears.put(player, swearMethod.getWords());
                }
            }
        }

        if (this.moderationManager.isCapsModerationEnabled()) {
            CapsModerationMethod capsMethod = this.moderationManager.getCapsMethod(message);
            if (!player.hasPermission("chatty.moderation.caps")) {
                if (capsMethod.isBlocked()) {

                    message = capsMethod.getEditedMessage();
                    if (capsMethod.isUseBlock()) {
                        if (configuration.getNode("general.completely-cancel").getAsBoolean(false))
                            cancelledByModeration = true;
                        else {
                            playerChatEvent.getRecipients().clear();
                            playerChatEvent.getRecipients().add(player);
                        }

                        logPrefixBuilder.append("[CAPS] ");
                    }

                    String capsFound = Chatty.instance().messages().get("caps-found", null);

                    if (capsFound != null)
                        Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                                () -> player.sendMessage(capsFound), 5L);
                }
            }
        }

        if (this.moderationManager.isAdvertisementModerationEnabled()) {
            AdvertisementModerationMethod advertisementMethod = this.moderationManager.getAdvertisementMethod(message);
            if (!player.hasPermission("chatty.moderation.advertisement")) {
                if (advertisementMethod.isBlocked()) {
                    message = advertisementMethod.getEditedMessage();

                    if (advertisementMethod.isUseBlock()) {
                        if (configuration.getNode("general.completely-cancel").getAsBoolean(false))
                            cancelledByModeration = true;
                        else {
                            playerChatEvent.getRecipients().clear();
                            playerChatEvent.getRecipients().add(player);
                        }

                        logPrefixBuilder.append("[ADS] ");
                    }

                    String adsFound = Chatty.instance().messages().get("advertisement-found", null);

                    if (adsFound != null)
                        Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                                () -> player.sendMessage(adsFound), 5L);
                }
            }
        }

        playerChatEvent.setMessage(message);

        if (cancelledByModeration) {
            playerChatEvent.setCancelled(true);
        } else {
            if (json) {
                pendingJsonMessages.put(player, chat);
            } else {
                if (configuration.getNode("general.bungeecord").getAsBoolean(false) && chat.getRange() <= -3) {
                    BungeeBroadcaster.broadcast(chat.getName(), String.format(playerChatEvent.getFormat(), player.getName(), message), false);
                }
            }
        }

        pendingSpyMessages.put(player, new Pair<>(chat, new ArrayList<>(playerChatEvent.getRecipients())));
        this.chatManager.getLogger().write(player, message, logPrefixBuilder.toString());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpyMessage(AsyncPlayerChatEvent playerChatEvent) {
        if (configuration.getNode("spy.enable").getAsBoolean(false)) {
            Pair<Chat, List<Player>> pair = pendingSpyMessages.remove(playerChatEvent.getPlayer());

            if (!playerChatEvent.isCancelled() && pair != null) {
                String spyInfo = COLORIZE.apply(configuration.getNode("spy.format.chat")
                        .getAsString("&6[Spy] &r{format}")
                        .replace("{format}", String.format(
                                playerChatEvent.getFormat(),
                                playerChatEvent.getPlayer().getName(),
                                playerChatEvent.getMessage())));

                Reflection.getOnlinePlayers().stream().
                        filter(spy ->
                            (spy.hasPermission("chatty.spy") || spy.hasPermission("chatty.spy." + pair.getKey().getName()))
                            && jsonStorage.getProperty(spy, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean()
                            && !pair.getValue().contains(spy))
                        .forEach(spy -> spy.sendMessage(spyInfo));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJsonMessage(AsyncPlayerChatEvent playerChatEvent) {
        if (!configuration.getNode("json.enable").getAsBoolean(false)) return;

        Chat chat = pendingJsonMessages.remove(playerChatEvent.getPlayer());

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
        String suggestCommand = configuration.getNode("json.suggest").getAsString(null);
        String link = configuration.getNode("json.link").getAsString(null);

        Function<String, String> replaceVariables = string -> {
            if (string == null) return null;

            string = string.replace("{player}", player.getDisplayName());

            if (placeholderAPI != null)
                string = placeholderAPI.setPlaceholders(player, string);

            return string;
        };

        FormattedMessage formattedMessage = new FormattedMessage(format);
        formattedMessage.replace("{player}",
                new JSONMessagePart(player.getDisplayName())
                    .command(replaceVariables.apply(command))
                    .suggest(replaceVariables.apply(suggestCommand))
                    .link(replaceVariables.apply(link))
                    .tooltip(tooltip));

        configuration.getNode("json.replacements").getChildNodes().forEach(replacement -> {
            String replacementName = replacement.getNode("original").getAsString(replacement.getName());

            String text = replacement.getNode("text").getAsString(replacementName);
            List<String> replacementTooltip = replacement.getNode("tooltip").getAsStringList();

            replacementTooltip = replacementTooltip.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("{player}", player.getDisplayName())))
                    .collect(Collectors.toList());

            if (placeholderAPI != null)
                replacementTooltip = placeholderAPI.setPlaceholders(player, replacementTooltip);

            String replacementCommand = replacement.getNode("command").getAsString(null);
            String replacementSuggestCommand = replacement.getNode("suggest").getAsString(null);
            String replacementLink = replacement.getNode("link").getAsString(null);

            formattedMessage.replace(replacementName, new JSONMessagePart(replaceVariables.apply(text))
                    .command(replaceVariables.apply(replacementCommand))
                    .suggest(replaceVariables.apply(replacementSuggestCommand))
                    .link(replaceVariables.apply(replacementLink))
                    .tooltip(replacementTooltip));
        });

        formattedMessage.replace("{message}", new LegacyMessagePart(playerChatEvent.getMessage(), false));

        if (configuration.getNode("general.bungeecord").getAsBoolean(false)) {
            BungeeBroadcaster.broadcast(chat.getName(), formattedMessage.toJSONString(), true);
        }

        if (configuration.getNode("json.swears.enable").getAsBoolean(false)) {
            String replacement = configuration.getNode("moderation.swear.replacement").getAsString("<swear>");
            List<String> swears = pendingSwears.remove(playerChatEvent.getPlayer());

            if (swears == null) {
                formattedMessage.send(playerChatEvent.getRecipients());
            } else {
                List<Player> canSeeSwears = new ArrayList<>();
                List<Player> cannotSeeSwears = new ArrayList<>();

                Reflection.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (onlinePlayer.hasPermission("chatty.swears.see")) {
                        canSeeSwears.add(onlinePlayer);
                    } else {
                        cannotSeeSwears.add(onlinePlayer);
                    }
                });

                formattedMessage.send(cannotSeeSwears);

                List<String> swearTooltip = configuration.getNode("json.swears.tooltip").getAsStringList()
                        .stream().map(tooltipLine -> ChatColor.translateAlternateColorCodes('&', tooltipLine)).collect(Collectors.toList());

                String suggest = configuration.getNode("json.swears.suggest").getAsString(null);

                swears.forEach(swear -> formattedMessage.replace(replacement,
                        new JSONMessagePart(replacement)
                                .tooltip(swearTooltip.stream().map(tooltipLine -> tooltipLine.replace("{word}", swear)).collect(Collectors.toList()))
                                .suggest(suggest != null ? suggest.replace("{word}", swear) : null)));

                formattedMessage.send(canSeeSwears);
            }
        } else {
            formattedMessage.send(playerChatEvent.getRecipients());
        }

        playerChatEvent.getRecipients().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        if (!configuration.getNode("miscellaneous.vanilla.join.enable").getAsBoolean(false)) {
            return;
        }

        String joinMessage = configuration
                .getNode("miscellaneous.vanilla.join.message")
                .getAsString(null);

        if (joinMessage != null) {
            if (joinMessage.isEmpty() ||
                    (configuration.getNode("miscellaneous.vanilla.join.permission")
                            .getAsBoolean(false)
                            && !playerJoinEvent.getPlayer().hasPermission("chatty.misc.joinmessage"))) {
                playerJoinEvent.setJoinMessage(null);
            } else playerJoinEvent.setJoinMessage(COLORIZE.apply(joinMessage
                    .replace("{prefix}", prefixAndSuffixManager.getPrefix(playerJoinEvent.getPlayer()))
                    .replace("{suffix}", prefixAndSuffixManager.getSuffix(playerJoinEvent.getPlayer()))
                    .replace("{player}", playerJoinEvent.getPlayer().getDisplayName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        if (!configuration.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false)) {
            return;
        }

        String quitMessage = configuration
                .getNode("miscellaneous.vanilla.quit.message")
                .getAsString(null);

        if (quitMessage != null) {
            if (quitMessage.isEmpty() ||
                    (configuration.getNode("miscellaneous.vanilla.quit.permission")
                            .getAsBoolean(false)
                            && !playerQuitEvent.getPlayer().hasPermission("chatty.misc.quitmessage"))) {
                playerQuitEvent.setQuitMessage(null);
            } else playerQuitEvent.setQuitMessage(COLORIZE.apply(quitMessage
                    .replace("{prefix}", prefixAndSuffixManager.getPrefix(playerQuitEvent.getPlayer()))
                    .replace("{suffix}", prefixAndSuffixManager.getSuffix(playerQuitEvent.getPlayer()))
                    .replace("{player}", playerQuitEvent.getPlayer().getDisplayName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (!configuration.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false)) {
            return;
        }

        String deathMessage = configuration
                .getNode("miscellaneous.vanilla.death.message")
                .getAsString(null);

        if (deathMessage != null) {
            if (deathMessage.isEmpty() ||
                    (configuration.getNode("miscellaneous.vanilla.death.permission")
                            .getAsBoolean(false)
                            && !playerDeathEvent.getEntity().hasPermission("chatty.misc.deathmessage"))) {
                playerDeathEvent.setDeathMessage(null);
            } else playerDeathEvent.setDeathMessage(COLORIZE.apply(deathMessage
                    .replace("{prefix}", prefixAndSuffixManager.getPrefix(playerDeathEvent.getEntity()))
                    .replace("{suffix}", prefixAndSuffixManager.getSuffix(playerDeathEvent.getEntity()))
                    .replace("{player}", playerDeathEvent.getEntity().getDisplayName())));
        }
    }

    private Chat getChat(final Player player, final String message) {
        Chat currentChat = null;

        for (Chat chat : this.chatManager.getChats()) {
            if (!chat.isEnable()) {
                continue;
            }

            if (!chat.isPermission()
                    || player.hasPermission(String.format("chatty.chat.%s", chat.getName()))
                    || player.hasPermission(String.format("chatty.chat.%s.write", chat.getName()))) {
                if (chat.getSymbol().isEmpty()) {
                    currentChat = chat;
                } else if (message.startsWith(chat.getSymbol())) {
                    currentChat = chat;
                    break;
                }
            }
        }

        return currentChat;
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

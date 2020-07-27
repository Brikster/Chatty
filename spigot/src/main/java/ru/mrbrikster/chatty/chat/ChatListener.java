package ru.mrbrikster.chatty.chat;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
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
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.api.events.ChattyMessageEvent;
import ru.mrbrikster.chatty.bungee.BungeeBroadcaster;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlaceholderAPIHook;
import ru.mrbrikster.chatty.dependencies.PrefixAndSuffixManager;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.json.JsonMessagePart;
import ru.mrbrikster.chatty.json.LegacyMessagePart;
import ru.mrbrikster.chatty.moderation.AdvertisementModerationMethod;
import ru.mrbrikster.chatty.moderation.CapsModerationMethod;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.Pair;
import ru.mrbrikster.chatty.util.Sound;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatListener implements Listener, EventExecutor {

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)&([0-9A-F])");
    private static final Pattern MAGIC_PATTERN = Pattern.compile("(?i)&([K])");
    private static final Pattern BOLD_PATTERN = Pattern.compile("(?i)&([L])");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("(?i)&([M])");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("(?i)&([N])");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?i)&([O])");
    private static final Pattern RESET_PATTERN = Pattern.compile("(?i)&([R])");
    private static final String PERMISSION_PREFIX = "chatty.style.";

    private static final Map<String, Pattern> PATTERNS = ImmutableMap.<String, Pattern>builder()
            .put(PERMISSION_PREFIX + "colors", COLOR_PATTERN)
            .put(PERMISSION_PREFIX + "magic", MAGIC_PATTERN)
            .put(PERMISSION_PREFIX + "bold", BOLD_PATTERN)
            .put(PERMISSION_PREFIX + "strikethrough", STRIKETHROUGH_PATTERN)
            .put(PERMISSION_PREFIX + "underline", UNDERLINE_PATTERN)
            .put(PERMISSION_PREFIX + "italic", ITALIC_PATTERN)
            .put(PERMISSION_PREFIX + "reset", RESET_PATTERN).build();

    private static final Pattern MENTION_PATTERN = Pattern.compile("@[a-zA-Zа-яА-Я0-9_]{3,16}");

    private final DependencyManager dependencyManager;
    private final ChatManager chatManager;
    private final Configuration configuration;
    private final ModerationManager moderationManager;
    private final JsonStorage jsonStorage;
    private final PrefixAndSuffixManager prefixAndSuffixManager;
    
    private final IdentityHashMap<Player, Pair<Chat, List<Player>>> pendingSpyMessages;
    private final IdentityHashMap<Player, List<String>> pendingSwears;
    private final IdentityHashMap<Player, Chat> pendingJsonMessages;

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
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (listener == this && event instanceof AsyncPlayerChatEvent) {
            this.onChat((AsyncPlayerChatEvent) event);
        }
    }

    private void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        Pair<Chat, String> chatMessagePair = getChat(player, event.getMessage());
        Chat chat = chatMessagePair.getA();
        String message = chatMessagePair.getB();

        if (chat == null) {
            event.setCancelled(true);
            player.sendMessage(Chatty.instance().messages().get("chat-not-found"));
            return;
        }

        boolean json = configuration.getNode("json.enable").getAsBoolean(false);

        message = stylish(player, message, chat.getName());

        if (ChatColor.stripColor(message).isEmpty()) {
            event.setCancelled(true);
            return;
        }

        boolean hasCooldown = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown") ||
                player.hasPermission("chatty.cooldown." + chat.getName());
        long cooldown = hasCooldown ? -1 : chat.getCooldown(player);

        if (cooldown != -1) {
            player.sendMessage(Chatty.instance().messages().get("cooldown")
                    .replace("{cooldown}", String.valueOf(cooldown)));
            event.setCancelled(true);
            return;
        }

        if (chat.getMoney() > 0 && dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();

            if (!vaultHook.withdrawMoney(player, chat.getMoney())) {
                player.sendMessage(Chatty.instance().messages().get("not-enough-money")
                        .replace("{money}", String.valueOf(chat.getMoney())));
                event.setCancelled(true);
                return;
            }
        }

        String format = chat.getFormat();

        format = TextUtil.fixMultilineFormatting(format);

        format = format.replace("{prefix}", prefixAndSuffixManager.getPrefix(player));
        format = format.replace("{suffix}", prefixAndSuffixManager.getSuffix(player));

        if (dependencyManager.getPlaceholderApi() != null) {
            format = dependencyManager.getPlaceholderApi().setPlaceholders(player, format);
        }

        format = TextUtil.stylish(format);

        format = format.replace("%", "%%");
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");

        event.setFormat(format);

        event.getRecipients().clear();
        event.getRecipients().addAll(chat.getRecipients(player));

        if (event.getRecipients().size() <= 1) {
            String noRecipients = Chatty.instance().messages().get("no-recipients", null);

            if (noRecipients != null && chat.getRange() > -3) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () -> player.sendMessage(noRecipients), 5L);
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
                            event.getRecipients().clear();
                            event.getRecipients().add(player);
                        }

                        logPrefixBuilder.append("[SWEAR] ");
                    }

                    String swearFound = Chatty.instance().messages().get("swear-found", null);

                    if (swearFound != null)
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () -> player.sendMessage(swearFound), 5L);
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
                            event.getRecipients().clear();
                            event.getRecipients().add(player);
                        }

                        logPrefixBuilder.append("[CAPS] ");
                    }

                    String capsFound = Chatty.instance().messages().get("caps-found", null);

                    if (capsFound != null)
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () -> player.sendMessage(capsFound), 5L);
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
                            event.getRecipients().clear();
                            event.getRecipients().add(player);
                        }

                        logPrefixBuilder.append("[ADS] ");
                    }

                    String adsFound = Chatty.instance().messages().get("advertisement-found", null);

                    if (adsFound != null)
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () -> player.sendMessage(adsFound), 5L);
                }
            }
        }

        event.setMessage(message);

        if (cancelledByModeration) {
            event.setCancelled(true);
        } else {
            if (json) {
                pendingJsonMessages.put(player, chat);
            } else {
                if (configuration.getNode("general.bungeecord").getAsBoolean(false) && chat.getRange() <= -3) {
                    BungeeBroadcaster.broadcast(event.getPlayer(), chat.getName(), String.format(event.getFormat(), player.getName(), message), false);
                }
            }
        }

        pendingSpyMessages.put(player, Pair.of(chat, new ArrayList<>(event.getRecipients())));

        if (configuration.getNode("general.log").getAsBoolean(false)) {
            this.chatManager.getLogger().write(player, message, logPrefixBuilder.toString());
        }

        ChattyMessageEvent chattyMessageEvent = new ChattyMessageEvent(player, chat, message);
        Bukkit.getPluginManager().callEvent(chattyMessageEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpyMessage(AsyncPlayerChatEvent event) {
        if (configuration.getNode("spy.enable").getAsBoolean(false)) {
            Pair<Chat, List<Player>> pair = pendingSpyMessages.remove(event.getPlayer());

            if (!event.isCancelled() && pair != null) {
                String spyInfo = TextUtil.stylish(configuration.getNode("spy.format.chat")
                        .getAsString("&6[Spy] &r{format}")
                        .replace("{format}", String.format(
                                event.getFormat(),
                                event.getPlayer().getName(),
                                event.getMessage())));

                Reflection.getOnlinePlayers().stream().
                        filter(spy ->
                                (spy.hasPermission("chatty.spy." + pair.getA().getName()))
                                    && jsonStorage.getProperty(spy, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean()
                                    && !pair.getB().contains(spy))
                        .forEach(spy -> spy.sendMessage(spyInfo));
            }
        }
    }

    /**
     * Method handles AsyncPlayerChatEvent with MONITOR priority
     * It let the other plugins handle the event then cancel it
     * @param event AsyncPlayerChatEvent object
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJsonMessage(AsyncPlayerChatEvent event) {
        if (configuration.getNode("json.enable").getAsBoolean(false)) {
            performJsonMessage(event);
        } else {
            String format = String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage());
            String strippedHexFormat = TextUtil.stripHex(format);

            if (!strippedHexFormat.equals(format)) {
                event.getRecipients().forEach(player -> player.sendMessage(format));
                event.getRecipients().clear();
                event.setFormat(strippedHexFormat);
            }
        }
    }

    private void performJsonMessage(AsyncPlayerChatEvent event) {
        Chat chat = pendingJsonMessages.remove(event.getPlayer());

        Player player = event.getPlayer();
        String format = unstylish(String.format(event.getFormat(), "{player}", "{message}"));

        PlaceholderAPIHook placeholderAPI = dependencyManager.getPlaceholderApi();
        List<String> tooltip = configuration.getNode("json.tooltip").getAsStringList()
                .stream().map(line -> ChatColor.translateAlternateColorCodes('&',
                        line.replace("{player}", player.getName())
                                .replace("{prefix}", prefixAndSuffixManager.getPrefix(player))
                                .replace("{suffix}", prefixAndSuffixManager.getSuffix(player))
                )).collect(Collectors.toList());

        if (placeholderAPI != null)
            tooltip = placeholderAPI.setPlaceholders(player, tooltip);

        String command = configuration.getNode("json.command").getAsString(null);
        String suggestCommand = configuration.getNode("json.suggest").getAsString(null);
        String link = configuration.getNode("json.link").getAsString(null);

        Function<String, String> stringVariablesFunction = createVariablesFunction(player);

        FormattedMessage formattedMessage = new FormattedMessage(format);
        formattedMessage.replace("{player}",
                new JsonMessagePart(player.getDisplayName())
                        .command(stringVariablesFunction.apply(command))
                        .suggest(stringVariablesFunction.apply(suggestCommand))
                        .link(stringVariablesFunction.apply(link))
                        .tooltip(tooltip));

        configuration.getNode("json.replacements").getChildNodes().forEach(replacement -> {
            String replacementName = replacement.getNode("original").getAsString(replacement.getName());

            String text = replacement.getNode("text").getAsString(replacementName);
            List<String> replacementTooltip = replacement.getNode("tooltip").getAsStringList();

            replacementTooltip = replacementTooltip.stream().map(line ->
                    ChatColor.translateAlternateColorCodes('&',
                            line.replace("{player}", player.getDisplayName())
                                    .replace("{prefix}", prefixAndSuffixManager.getPrefix(player))
                                    .replace("{suffix}", prefixAndSuffixManager.getSuffix(player))
                    )).collect(Collectors.toList());

            if (placeholderAPI != null)
                replacementTooltip = placeholderAPI.setPlaceholders(player, replacementTooltip);

            String replacementCommand = replacement.getNode("command").getAsString(null);
            String replacementSuggestCommand = replacement.getNode("suggest").getAsString(null);
            String replacementLink = replacement.getNode("link").getAsString(null);

            formattedMessage.replace(replacementName, new JsonMessagePart(stringVariablesFunction.apply(text))
                    .command(stringVariablesFunction.apply(replacementCommand))
                    .suggest(stringVariablesFunction.apply(replacementSuggestCommand))
                    .link(stringVariablesFunction.apply(replacementLink))
                    .tooltip(replacementTooltip));
        });

        if (configuration.getNode("json.mentions.enable").getAsBoolean(false) && player.hasPermission("chatty.mentions")) {
            FormattedMessage messageWithMention = new FormattedMessage(event.getMessage());
            Matcher matcher = MENTION_PATTERN.matcher(event.getMessage());

            while (matcher.find()) {
                String group = matcher.group();
                String playerName = group.substring(1);

                Player mentionedPlayer;
                if ((mentionedPlayer = Bukkit.getPlayer(playerName)) != null) {
                    if (mentionedPlayer.equals(event.getPlayer())) {
                        continue;
                    }

                    Function<String, String> mentionedPlayerVariablesFunc = createVariablesFunction(mentionedPlayer);

                    List<String> mentionTooltip = configuration.getNode("json.mentions.tooltip").getAsStringList();
                    mentionTooltip = mentionTooltip.stream().map(line ->
                            ChatColor.translateAlternateColorCodes('&',
                                    line.replace("{player}", mentionedPlayer.getDisplayName())
                                            .replace("{prefix}", prefixAndSuffixManager.getPrefix(mentionedPlayer))
                                            .replace("{suffix}", prefixAndSuffixManager.getSuffix(mentionedPlayer))
                            )).collect(Collectors.toList());

                    if (placeholderAPI != null) {
                        mentionTooltip = placeholderAPI.setPlaceholders(mentionedPlayer, mentionTooltip);
                    }

                    link = mentionedPlayerVariablesFunc.apply(configuration.getNode("json.mentions.link").getAsString(null));
                    suggestCommand = mentionedPlayerVariablesFunc.apply(configuration.getNode("json.mentions.suggest").getAsString(null));
                    command = mentionedPlayerVariablesFunc.apply(configuration.getNode("json.mentions.command").getAsString(null));

                    playerName = mentionedPlayer.getDisplayName();

                    messageWithMention.replace(group,
                            new JsonMessagePart(applyPlaceholders(mentionedPlayer, configuration.getNode("json.mentions.format")
                                    .getAsString("&e&l@{player}").replace("{player}", playerName)))
                                    .tooltip(mentionTooltip).command(command).suggest(suggestCommand).link(link),
                            new LegacyMessagePart(TextUtil.getLastColors(event.getFormat())));

                    String soundName = configuration.getNode("json.mentions.sound").getAsString(null);
                    if (soundName != null) {
                        org.bukkit.Sound sound = Sound.byName(soundName);
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), sound, 1L, 1L);
                    }

                    event.getRecipients().add(mentionedPlayer);
                }
            }

            formattedMessage.replace("{message}", messageWithMention);
        } else {
            formattedMessage.replace("{message}", new LegacyMessagePart(event.getMessage(), false));
        }

        if (configuration.getNode("general.bungeecord").getAsBoolean(false)) {
            BungeeBroadcaster.broadcast(event.getPlayer(), chat.getName(), formattedMessage.toJSONString(), true);
        }

        if (configuration.getNode("json.swears.enable").getAsBoolean(false)) {
            String replacement = configuration.getNode("moderation.swear.replacement").getAsString("<swear>");
            List<String> swears = pendingSwears.remove(event.getPlayer());

            if (swears == null) {
                formattedMessage.send(event.getRecipients());
            } else {
                List<Player> canSeeSwears = new ArrayList<>();
                List<Player> cannotSeeSwears = new ArrayList<>();

                event.getRecipients().forEach(recipient -> {
                    if (recipient.hasPermission("chatty.swears.see")) {
                        canSeeSwears.add(recipient);
                    } else {
                        cannotSeeSwears.add(recipient);
                    }
                });

                formattedMessage.send(cannotSeeSwears);

                List<String> swearTooltip = configuration.getNode("json.swears.tooltip").getAsStringList()
                        .stream().map(TextUtil::stylish).collect(Collectors.toList());

                String suggest = configuration.getNode("json.swears.suggest").getAsString(null);

                swears.forEach(swear -> formattedMessage.replace(replacement,
                        new JsonMessagePart(replacement)
                                .tooltip(swearTooltip.stream().map(tooltipLine -> tooltipLine.replace("{word}", swear))
                                        .collect(Collectors.toList()))
                                .suggest(suggest != null ? suggest.replace("{word}", swear) : null)));

                formattedMessage.send(canSeeSwears);
            }
        } else {
            formattedMessage.send(event.getRecipients());
        }

        event.setFormat(formattedMessage.toReadableText().replace("%", "%%"));
        event.getRecipients().clear();

        format = String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage());
        String strippedHexFormat = TextUtil.stripHex(format);

        if (!strippedHexFormat.equals(format)) {
            event.setFormat(strippedHexFormat);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (!configuration.getNode("miscellaneous.vanilla.join.enable").getAsBoolean(false)) {
            return;
        }

        String joinMessage = configuration
                .getNode("miscellaneous.vanilla.join.message")
                .getAsString(null);

        boolean hasPermission = !configuration.getNode("miscellaneous.vanilla.join.permission").getAsBoolean(true)
                || event.getPlayer().hasPermission("chatty.misc.joinmessage");

        if (joinMessage != null) {
            if (joinMessage.isEmpty() || !hasPermission) {
                event.setJoinMessage(null);
            } else event.setJoinMessage(TextUtil.stylish(
                    applyPlaceholders(event.getPlayer(),
                            joinMessage.replace("{prefix}", prefixAndSuffixManager.getPrefix(event.getPlayer()))
                                .replace("{suffix}", prefixAndSuffixManager.getSuffix(event.getPlayer()))
                                .replace("{player}", event.getPlayer().getDisplayName()))));
        }

        if (hasPermission) {
            String soundName = configuration.getNode("miscellaneous.vanilla.join.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1L, 1L));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!configuration.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false)) {
            return;
        }

        String quitMessage = configuration
                .getNode("miscellaneous.vanilla.quit.message")
                .getAsString(null);

        boolean hasPermission = !configuration.getNode("miscellaneous.vanilla.quit.permission").getAsBoolean(true)
                || event.getPlayer().hasPermission("chatty.misc.quitmessage");

        if (quitMessage != null) {
            if (quitMessage.isEmpty() || !hasPermission) {
                event.setQuitMessage(null);
            } else event.setQuitMessage(TextUtil.stylish(
                    applyPlaceholders(event.getPlayer(),
                            quitMessage.replace("{prefix}", prefixAndSuffixManager.getPrefix(event.getPlayer()))
                                    .replace("{suffix}", prefixAndSuffixManager.getSuffix(event.getPlayer()))
                                    .replace("{player}", event.getPlayer().getDisplayName()))));
        }

        if (hasPermission) {
            String soundName = configuration.getNode("miscellaneous.vanilla.quit.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1L, 1L));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configuration.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false)) {
            return;
        }

        String deathMessage = configuration
                .getNode("miscellaneous.vanilla.death.message")
                .getAsString(null);

        boolean hasPermission = !configuration.getNode("miscellaneous.vanilla.death.permission").getAsBoolean(true)
                || event.getEntity().hasPermission("chatty.misc.deathmessage");

        if (deathMessage != null) {
            if (deathMessage.isEmpty() || !hasPermission) {
                event.setDeathMessage(null);
            } else event.setDeathMessage(TextUtil.stylish(
                    applyPlaceholders(event.getEntity(),
                            deathMessage.replace("{prefix}", prefixAndSuffixManager.getPrefix(event.getEntity()))
                                    .replace("{suffix}", prefixAndSuffixManager.getSuffix(event.getEntity()))
                                    .replace("{player}", event.getEntity().getDisplayName()))));
        }

        if (hasPermission) {
            String soundName = configuration.getNode("miscellaneous.vanilla.death.sound").getAsString(null);
            if (soundName != null) {
                org.bukkit.Sound sound = Sound.byName(soundName);
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1L, 1L));
            }
        }
    }

    private Pair<Chat, String> getChat(final Player player, String message) {
        Chat currentChat = null;

        Optional<JsonElement> optional = jsonStorage.getProperty(player, "chat");
        if (optional.isPresent()) {
            JsonElement jsonElement = optional.get();
            if (jsonElement.isJsonPrimitive()) {
                String chatName = jsonElement.getAsJsonPrimitive().getAsString();
                Chat chat = chatManager.getChat(chatName);
                if (chat != null) {
                    if (chat.isAllowed(player)) {
                        currentChat = chat;
                    }
                }
            }
        }

        for (Chat chat : this.chatManager.getChats()) {
            if (chat.isAllowed(player)) {
                if (chat.getSymbol().isEmpty()) {
                    if (currentChat == null) {
                        currentChat = chat;
                    }
                } else if (message.startsWith(chat.getSymbol())) {
                    if (!chat.equals(currentChat)) {
                        message = message.substring(chat.getSymbol().length());
                    }

                    currentChat = chat;
                    break;
                }
            }
        }

        return Pair.of(currentChat, message.trim());
    }

    private String stylish(Player player, String message, String chat) {
        for (Map.Entry<String, Pattern> entry : PATTERNS.entrySet()) {
            if (player.hasPermission(entry.getKey()) || player.hasPermission(entry.getKey() + "." + chat)) {
                message = entry.getValue().matcher(message).replaceAll("\u00A7$1");
            }
        }

        return message;
    }

    private String applyPlaceholders(Player player, String string) {
        if (dependencyManager.getPlaceholderApi() != null) {
            string = dependencyManager.getPlaceholderApi().setPlaceholders(player, string);
        }

        return string;
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

    private Function<String, String> createVariablesFunction(Player player) {
        return string -> {
            if (string == null) return null;

            string = string.replace("{player}", player.getDisplayName());
            string = string.replace("{prefix}", prefixAndSuffixManager.getPrefix(player));
            string = string.replace("{suffix}", prefixAndSuffixManager.getSuffix(player));

            return ChatColor.stripColor(applyPlaceholders(player, string));
        };
    }

}

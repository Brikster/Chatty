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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.api.events.ChattyMessageEvent;
import ru.mrbrikster.chatty.bungee.BungeeBroadcaster;
import ru.mrbrikster.chatty.chat.event.ChattyAsyncPlayerChatEvent;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlayerTagManager;
import ru.mrbrikster.chatty.dependencies.VaultHook;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.json.JsonMessagePart;
import ru.mrbrikster.chatty.json.LegacyMessagePart;
import ru.mrbrikster.chatty.moderation.*;
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
    private final PlayerTagManager playerTagManager;

    private final Map<Player, Pair<Chat, List<Player>>> pendingSpyMessages;
    private final Map<Player, List<String>> pendingSwears;
    private final Map<Player, Chat> pendingMessages;

    public ChatListener(Chatty chatty) {
        this.configuration = chatty.getExact(Configuration.class);
        this.chatManager = chatty.getExact(ChatManager.class);
        this.dependencyManager = chatty.getExact(DependencyManager.class);
        this.moderationManager = chatty.getExact(ModerationManager.class);
        this.jsonStorage = chatty.getExact(JsonStorage.class);
        this.playerTagManager = chatty.getExact(PlayerTagManager.class);

        this.pendingSpyMessages = new IdentityHashMap<>();
        this.pendingSwears = new IdentityHashMap<>();
        this.pendingMessages = new IdentityHashMap<>();
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (listener == this && event instanceof AsyncPlayerChatEvent) {
            if (((AsyncPlayerChatEvent) event).isCancelled()) {
                return;
            }

            this.onChat((AsyncPlayerChatEvent) event);
        }
    }

    private void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        Chat chat;
        String message;

        if (event instanceof ChattyAsyncPlayerChatEvent) {
            chat = ((ChattyAsyncPlayerChatEvent) event).getChat();
            message = event.getMessage();
        } else {
            Pair<Chat, String> chatMessagePair = getChat(player, event.getMessage());
            chat = chatMessagePair.getA();
            message = chatMessagePair.getB();
        }

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

        if (hasActiveCooldown(event, player, chat)) return;

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

        format = format.replace("{prefix}", playerTagManager.getPrefix(player));
        format = format.replace("{suffix}", playerTagManager.getSuffix(player));

        if (dependencyManager.getPlaceholderApi() != null) {
            format = dependencyManager.getPlaceholderApi().setPlaceholders(player, format);
        }

        format = TextUtil.stylish(format);

        format = format.replace("%", "%%");
        format = format.replace("{player}", "%1$s");
        format = format.replace("{message}", "%2$s");

        event.setFormat(format);

        if (configuration.getNode("general.keep-old-recipients").getAsBoolean(true)) {
            chat.filterRecipients(player, event.getRecipients());
        } else {
            event.getRecipients().clear();
            event.getRecipients().addAll(chat.getRecipients(player));
        }

        long recipientsCount;
        if (configuration.getNode("general.hide-vanished-recipients").getAsBoolean(false)) {
            recipientsCount = event.getRecipients().stream().filter(player::canSee).count();
        } else {
            recipientsCount = event.getRecipients().size();
        }

        if (recipientsCount <= 1) {
            String noRecipients = Chatty.instance().messages().get("no-recipients", null);

            if (noRecipients != null && chat.getRange() > -3) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () -> player.sendMessage(noRecipients), 5L);
            }
        }

        StringBuilder logPrefixBuilder = new StringBuilder();
        message = checkModerationMethods(event, player, chat, message, logPrefixBuilder, TextUtil.getLastColors(format));

        event.setMessage(message);

        if (configuration.getNode("general.log").getAsBoolean(false)) {
            this.chatManager.getLogger().write(player, message, logPrefixBuilder.toString());
        }

        if (!event.isCancelled()) {
            pendingMessages.put(player, chat);

            if (!json) {
                if (configuration.getNode("general.bungeecord").getAsBoolean(false) && chat.getRange() <= -3) {
                    BungeeBroadcaster.broadcast(event.getPlayer(), chat.getName(), String.format(event.getFormat(), player.getName(), message), false);
                }
            }
        }

        pendingSpyMessages.put(player, Pair.of(chat, new ArrayList<>(event.getRecipients())));

        Bukkit.getScheduler().runTaskAsynchronously(Chatty.instance(), () -> {
            ChattyMessageEvent chattyMessageEvent = new ChattyMessageEvent(player, chat, event.getMessage());
            Bukkit.getPluginManager().callEvent(chattyMessageEvent);
        });
    }

    private boolean hasActiveCooldown(AsyncPlayerChatEvent event, Player player, Chat chat) {
        boolean bypassCooldown = chat.getCooldown() == -1 || player.hasPermission("chatty.cooldown." + chat.getName());
        long cooldown = bypassCooldown ? -1 : chat.getCooldown(player);

        if (cooldown != -1) {
            player.sendMessage(Chatty.instance().messages().get("cooldown")
                    .replace("{cooldown}", String.valueOf(cooldown)));
            event.setCancelled(true);
            return true;
        }

        if (!bypassCooldown) {
            chat.setCooldown(player);
        }

        return false;
    }

    private String checkModerationMethods(AsyncPlayerChatEvent event, Player player, Chat chat, String message, StringBuilder logPrefixBuilder, String lastFormatColors) {
        if (chat.isSwearModerationEnabled() && moderationManager.isSwearModerationEnabled()) {
            SwearModerationMethod swearMethod = moderationManager.getSwearMethod(message, lastFormatColors);
            if (!player.hasPermission("chatty.moderation.swear")) {
                message = handleModerationMethod(event, player, message, logPrefixBuilder, swearMethod);

                if (configuration.getNode("json.enable").getAsBoolean(false)
                        && configuration.getNode("json.swears.enable").getAsBoolean(false)) {
                    pendingSwears.put(player, swearMethod.getWords());
                }
            }
        }

        if (chat.isCapsModerationEnabled() && this.moderationManager.isCapsModerationEnabled()) {
            CapsModerationMethod capsMethod = this.moderationManager.getCapsMethod(message);
            if (!player.hasPermission("chatty.moderation.caps")) {
                message = handleModerationMethod(event, player, message, logPrefixBuilder, capsMethod);
            }
        }

        if (chat.isAdvertisementModerationEnabled() && this.moderationManager.isAdvertisementModerationEnabled()) {
            AdvertisementModerationMethod advertisementMethod = this.moderationManager.getAdvertisementMethod(message, lastFormatColors);
            if (!player.hasPermission("chatty.moderation.advertisement")) {
                message = handleModerationMethod(event, player, message, logPrefixBuilder, advertisementMethod);
            }
        }
        return message;
    }

    private String handleModerationMethod(AsyncPlayerChatEvent event, Player player, String message,
                                          StringBuilder logPrefixBuilder, ModerationMethod method) {
        if (method.isBlocked()) {
            message = method.getEditedMessage();
            if (method.isUseBlock()) {
                if (configuration.getNode("general.completely-cancel").getAsBoolean(false))
                    event.setCancelled(true);
                else {
                    event.getRecipients().clear();
                    event.getRecipients().add(player);
                }

                logPrefixBuilder.append("[").append(method.getLogPrefix()).append("] ");
            }

            String warningMessage = Chatty.instance().messages().get(method.getWarningMessageKey(), null);

            if (warningMessage != null)
                Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(), () -> player.sendMessage(warningMessage), 5L);
        }

        return message;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpyMessage(AsyncPlayerChatEvent event) {
        Pair<Chat, List<Player>> pair = pendingSpyMessages.remove(event.getPlayer());

        if (pair == null) return;

        if (pair.getA().isSpyEnabled()
                && configuration.getNode("spy.enable").getAsBoolean(false)
                && !event.isCancelled()) {
            String spyInfo = TextUtil.stylish(configuration.getNode("spy.format.chat")
                    .getAsString("&6[Spy] &r{format}")
                    .replace("{format}", String.format(
                            event.getFormat(),
                            event.getPlayer().getDisplayName(),
                            event.getMessage())));

            Bukkit.getOnlinePlayers().stream()
                    .filter(spy ->
                            (spy.hasPermission("chatty.spy." + pair.getA().getName()))
                                    && jsonStorage.getProperty(spy, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean()
                                    && !pair.getB().contains(spy))
                    .forEach(spy -> spy.sendMessage(spyInfo));
        }
    }

    /**
     * Method handles AsyncPlayerChatEvent with MONITOR priority
     * It let the other plugins handle the event then cancel it
     *
     * @param event AsyncPlayerChatEvent object
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatMonitor(AsyncPlayerChatEvent event) {
        Chat chat = pendingMessages.remove(event.getPlayer());

        if (chat == null) {
            // Seems to event was uncancelled by another plugin
            return;
        }

        if (chat.getSound() != null) {
            for (Player recipient : event.getRecipients()) {
                if (!recipient.equals(event.getPlayer())) {
                    recipient.playSound(recipient.getLocation(), chat.getSound(), 1L, 1L);
                }
            }
        }

        if (configuration.getNode("json.enable").getAsBoolean(false)) {
            performJsonMessage(event, chat);
        } else {
            event.setFormat(TextUtil.stripHex(event.getFormat()));
            String format = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
            event.getRecipients().forEach(player -> player.sendMessage(format));
            event.getRecipients().clear();
        }
    }

    private void performJsonMessage(AsyncPlayerChatEvent event, Chat chat) {
        Player player = event.getPlayer();
        String format = unstylish(String.format(event.getFormat(), "{player}", "{message}"));

        List<String> tooltip = configuration.getNode("json.tooltip").getAsStringList()
                .stream().map(line -> TextUtil.stylish(
                        line.replace("{player}", player.getDisplayName())
                                .replace("{prefix}", playerTagManager.getPrefix(player))
                                .replace("{suffix}", playerTagManager.getSuffix(player))
                )).collect(Collectors.toList());

        if (dependencyManager.getPlaceholderApi() != null)
            tooltip = dependencyManager.getPlaceholderApi().setPlaceholders(player, tooltip);

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

        configuration.getNode("json.replacements").getChildNodes().forEach(replacement ->
                applyReplacement(player, formattedMessage, replacement));

        if (configuration.getNode("json.mentions.enable").getAsBoolean(false) && player.hasPermission("chatty.mentions")) {
            applyMentions(event, formattedMessage);
        } else {
            formattedMessage.replace("{message}", new LegacyMessagePart(event.getMessage(), false));
        }

        if (configuration.getNode("general.bungeecord").getAsBoolean(false)) {
            BungeeBroadcaster.broadcast(event.getPlayer(), chat.getName(), formattedMessage.toJSONString(), true);
        }

        if (configuration.getNode("json.swears.enable").getAsBoolean(false)) {
            applyJsonSwears(event, formattedMessage);
        } else {
            formattedMessage.send(event.getRecipients(), event.getPlayer());
        }

        event.setFormat(formattedMessage.toReadableText().replace("%", "%%"));
        event.getRecipients().clear();

        format = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        String strippedHexFormat = TextUtil.stripHex(format);

        if (!strippedHexFormat.equals(format)) {
            try {
                event.setFormat(strippedHexFormat);
            }
            catch (UnknownFormatConversionException exception) {
                //do nothing if strippedHexFormat is broken
            }
        }
    }

    private void applyJsonSwears(AsyncPlayerChatEvent event, FormattedMessage formattedMessage) {
        String replacement = configuration.getNode("moderation.swear.replacement").getAsString("<swear>");
        List<String> swears = pendingSwears.remove(event.getPlayer());

        if (swears == null) {
            formattedMessage.send(event.getRecipients(), event.getPlayer());
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

            formattedMessage.send(cannotSeeSwears, event.getPlayer());

            List<String> swearTooltip = configuration.getNode("json.swears.tooltip").getAsStringList()
                    .stream().map(TextUtil::stylish).collect(Collectors.toList());

            String suggest = configuration.getNode("json.swears.suggest").getAsString(null);

            swears.forEach(swear -> formattedMessage.replace(replacement,
                    new JsonMessagePart(replacement)
                            .tooltip(swearTooltip.stream().map(tooltipLine -> tooltipLine.replace("{word}", swear))
                                    .collect(Collectors.toList()))
                            .suggest(suggest != null ? suggest.replace("{word}", swear) : null)));

            formattedMessage.send(canSeeSwears, event.getPlayer());
        }
    }

    private void applyMentions(AsyncPlayerChatEvent event, FormattedMessage formattedMessage) {
        String link;
        String suggestCommand;
        String command;
        FormattedMessage messageWithMention = new FormattedMessage(event.getMessage(), false);
        Matcher matcher = MENTION_PATTERN.matcher(event.getMessage());

        while (matcher.find()) {
            String group = matcher.group();
            String playerName = group.substring(1);

            Player mentionedPlayer;
            if ((mentionedPlayer = Bukkit.getPlayer(playerName)) != null) {
                if (mentionedPlayer.equals(event.getPlayer())) {
                    continue;
                }

                if (!event.getPlayer().canSee(mentionedPlayer)
                        && !configuration.getNode("json.mentions.allow-vanished").getAsBoolean(true)) {
                    continue;
                }

                Function<String, String> mentionedPlayerVariablesFunc = createVariablesFunction(mentionedPlayer);

                List<String> mentionTooltip = configuration.getNode("json.mentions.tooltip").getAsStringList();
                mentionTooltip = mentionTooltip.stream().map(line ->
                        TextUtil.stylish(
                                line.replace("{player}", mentionedPlayer.getDisplayName())
                                        .replace("{prefix}", playerTagManager.getPrefix(mentionedPlayer))
                                        .replace("{suffix}", playerTagManager.getSuffix(mentionedPlayer))
                        )).collect(Collectors.toList());

                if (dependencyManager.getPlaceholderApi() != null) {
                    mentionTooltip = dependencyManager.getPlaceholderApi().setPlaceholders(mentionedPlayer, mentionTooltip);
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
                if (!configuration.getNode("json.mentions.range-based-sound").getAsBoolean(false) | event.getRecipients().contains(mentionedPlayer)){
                    String soundName = configuration.getNode("json.mentions.sound").getAsString(null);
                    if (soundName != null) {
                        org.bukkit.Sound sound = Sound.byName(soundName);
                        double soundVolume = (double) configuration.getNode("json.mentions.sound-volume").get(1d);
                        double soundPitch = (double) configuration.getNode("json.mentions.sound-pitch").get(1d);
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), sound, (float) soundVolume, (float) soundPitch);
                    }

                    event.getRecipients().add(mentionedPlayer);
                }
            }
        }

        formattedMessage.replace("{message}", messageWithMention);
    }

    private void applyReplacement(Player player, FormattedMessage formattedMessage, ConfigurationNode replacement) {
        Function<String, String> stringVariablesFunction = createVariablesFunction(player);
        String replacementName = replacement.getNode("original").getAsString(replacement.getName());

        String text = replacement.getNode("text").getAsString(replacementName);
        List<String> replacementTooltip = replacement.getNode("tooltip").getAsStringList();

        replacementTooltip = replacementTooltip.stream().map(line ->
                TextUtil.stylish(
                        line.replace("{player}", player.getDisplayName())
                                .replace("{prefix}", playerTagManager.getPrefix(player))
                                .replace("{suffix}", playerTagManager.getSuffix(player))
                )).collect(Collectors.toList());

        if (dependencyManager.getPlaceholderApi() != null)
            replacementTooltip = dependencyManager.getPlaceholderApi().setPlaceholders(player, replacementTooltip);

        String replacementCommand = replacement.getNode("command").getAsString(null);
        String replacementSuggestCommand = replacement.getNode("suggest").getAsString(null);
        String replacementLink = replacement.getNode("link").getAsString(null);

        formattedMessage.replace(replacementName, new JsonMessagePart(stringVariablesFunction.apply(text))
                .command(stringVariablesFunction.apply(replacementCommand))
                .suggest(stringVariablesFunction.apply(replacementSuggestCommand))
                .link(stringVariablesFunction.apply(replacementLink))
                .tooltip(replacementTooltip));
    }

    private Pair<Chat, String> getChat(final Player player, String message) {
        Chat currentChat = this.chatManager.getCurrentChat(player);

        for (Chat chat : this.chatManager.getChats()) {
            if (chat.isWriteAllowed(player)) {
                if (chat.getSymbol().isEmpty()) {
                    if (currentChat == null) {
                        currentChat = chat;
                    }
                } else if (message.startsWith(chat.getSymbol())) {
                    if (!message.equals(chat.getSymbol())) {
                        if (!chat.equals(currentChat)) {
                            message = message.substring(chat.getSymbol().length());
                        }

                        currentChat = chat;
                    }
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
            if (b[i] == '\u00A7' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = '&';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    private Function<String, String> createVariablesFunction(Player player) {
        return string -> {
            if (string == null) return null;

            string = string.replace("{player}", player.getDisplayName());
            string = string.replace("{prefix}", playerTagManager.getPrefix(player));
            string = string.replace("{suffix}", playerTagManager.getSuffix(player));

            return ChatColor.stripColor(applyPlaceholders(player, string));
        };
    }

}

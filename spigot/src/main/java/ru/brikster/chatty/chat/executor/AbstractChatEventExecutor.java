package ru.brikster.chatty.chat.executor;

import ru.brikster.chatty.util.ChattyMessages;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.ChatStyle;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.context.MessageContextKeys;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.Stage;
import ru.brikster.chatty.api.event.ChattyMessageEvent;
import ru.brikster.chatty.api.event.ChattyPreMessageEvent;
import ru.brikster.chatty.chat.LastMessageState;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructor;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.transform.intermediary.IntermediateMessageTransformer;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessor;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.chat.style.ChatStylePlayerGrouper;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.misc.ChatLogWriter;
import ru.brikster.chatty.proxy.ProxyService;
import ru.brikster.chatty.util.EventUtil;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractChatEventExecutor implements Listener {

    private final Map<Integer, MessageContext<String>> pendingMessages = new ConcurrentHashMap<>();

    private static final long ERROR_LOG_INTERVAL_MILLIS = 10_000L;
    private final AtomicLong lastErrorLogTime = new AtomicLong(0L);
    private final AtomicInteger suppressedErrors = new AtomicInteger(0);

    @Inject protected ChatSelector selector;
    @Inject protected ComponentFromContextConstructor componentFromContextConstructor;
    @Inject protected BukkitAudiences audiences;
    @Inject protected SettingsConfig settings;
    @Inject protected ChatLogWriter chatLogWriter;
    @Inject protected MessagesConfig messages;
    @Inject protected MessageTransformStrategiesProcessor processor;
    @Inject protected IntermediateMessageTransformer intermediateMessageTransformer;
    @Inject protected Logger logger;
    @Inject protected ProxyService proxyService;
    @Inject protected ChatStylePlayerGrouper chatStylePlayerGrouper;
    @Inject protected LastMessageState lastMessageState;

    protected final void handleEarly(ChatEventFacade event, int eventHashcode) {
        boolean processed = false;

        try {
            long millisStart = System.currentTimeMillis();

            MessageContext<String> unhandledEarlyContext = createEarlyContext(event);
            MessageContext<String> earlyContext = processor.handle(unhandledEarlyContext, Stage.EARLY).getNewContext();

            event.clearRecipients();

            if (!earlyContext.isCancelled()) {
                event.setRecipients(earlyContext.getRecipients());
                event.setMessage(earlyContext.getMessage());
            }

            pendingMessages.put(eventHashcode, earlyContext);

            long millisEnd = System.currentTimeMillis();
            long millisDelta = millisEnd - millisStart;

            if (settings.isDebug()) {
                logger.log(Level.INFO, "Early context processed for " + millisDelta + "ms (event: " + eventHashcode + ")");
            }

            processed = true;
        } catch (Throwable t) {
            handleProcessingFailure(t, event.getPlayer());
        } finally {
            if (!processed) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Logs a chat-processing failure (rate-limited so a single recurring fault
     * cannot flood the console) and notifies the affected player.
     */
    private void handleProcessingFailure(Throwable t, Player player) {
        long now = System.currentTimeMillis();
        long last = lastErrorLogTime.get();
        if (now - last >= ERROR_LOG_INTERVAL_MILLIS && lastErrorLogTime.compareAndSet(last, now)) {
            int suppressed = suppressedErrors.getAndSet(0);
            String message = suppressed > 0
                    ? "Cannot handle chat event (" + suppressed + " similar error(s) suppressed in the last "
                            + (ERROR_LOG_INTERVAL_MILLIS / 1000) + "s)"
                    : "Cannot handle chat event";
            logger.log(Level.SEVERE, message, t);
        } else {
            suppressedErrors.incrementAndGet();
        }
        ChattyMessages.send(audiences.player(player),
                messages.getChatErrorOccurred());
    }

    private MessageContext<String> createEarlyContext(ChatEventFacade event) {
        Chat chat = selector.selectChat(event.getPlayer(), event.getMessage(), chatCandidate ->
                !chatCandidate.isPermissionRequired() ||
                        chatCandidate.hasSymbolWritePermission(event.getPlayer()));

        List<Player> recipients;
        if (chat == null) {
            ChattyMessages.send(audiences.player(event.getPlayer().getUniqueId()),
                    messages.getChatNotFound());
            recipients = Collections.emptyList();
            event.setCancelled(true);
        } else {
            if (settings.isRespectForeignRecipients()) {
                Predicate<Player> playerPredicate = chat.getRecipientPredicate(event.getPlayer());
                recipients = event.getRecipients().stream()
                        .filter(playerPredicate)
                        .collect(Collectors.toList());
            } else {
                recipients = new ArrayList<>(chat.calculateRecipients(event.getPlayer()));
            }
        }

        return new MessageContextImpl<>(
                chat,
                event.getPlayer(),
                new HashMap<>(),
                event.isCancelled(),
                chat == null ? Component.text("") : chat.getFormat(event.getPlayer()),
                chat == null ? "{original-message}" : chat.getMessageFormat(event.getPlayer()),
                recipients,
                event.getMessage(),
                null);
    }

    protected final void handleLate(ChatEventFacade event, int eventHashcode) {
        MessageContext<String> earlyContext = pendingMessages.remove(eventHashcode);
        if (earlyContext == null) {
            if (settings.isDebug()) {
                logger.log(Level.WARNING, "Cannot handle unprocessed chat event from \"{0}\" with format \"{1}\" and message \"{2}\"",
                        new Object[]{event.getPlayer().getName(), event.getDebugFormat(), event.getMessage()});
            }
            return;
        }

        if (earlyContext.isCancelled()) {
            event.setCancelled(true);
        }

        if (event.isCancelled()) return;

        List<Player> untouchedRecipients = event.getRecipients();

        earlyContext.setRecipients(new ArrayList<>(untouchedRecipients));
        earlyContext.setMessage(event.getMessage());

        event.clearRecipients();

        boolean processed = false;
        boolean delivered = false;
        boolean dropped = false;

        try {
            long millisStart = System.currentTimeMillis();

            MessageContext<Component> earlyComponentContext = intermediateMessageTransformer.handle(earlyContext).getNewContext();

            if (PlainTextComponentSerializer.plainText().serialize(earlyComponentContext.getMessage()).isBlank()) {
                dropped = true;
                return;
            }

            MessageContext<Component> middleContext = processor.handle(earlyComponentContext, Stage.MIDDLE).getNewContext();

            ChattyPreMessageEvent preMessageEvent = new ChattyPreMessageEvent(
                    middleContext.getSender(),
                    middleContext.getChat(),
                    middleContext.getChat().getStyles(middleContext.getSender()),
                    middleContext.getFormat(),
                    middleContext.getMessageFormat(),
                    middleContext.getMessage(),
                    List.copyOf(middleContext.getRecipients())
            );

            EventUtil.callAsynchronously(preMessageEvent);

            middleContext.setFormat(preMessageEvent.getFormat());
            middleContext.setMessageFormat(preMessageEvent.getMessageFormat());
            middleContext.setMessage(preMessageEvent.getMessage());

            rememberForPlaceholders(middleContext);

            Set<ChatStyle> styles = preMessageEvent.getStyles();

            ChattyMessageEvent messageEvent = new ChattyMessageEvent(
                    middleContext.getSender(),
                    middleContext.getChat(),
                    PlainTextComponentSerializer.plainText().serialize(preMessageEvent.getMessage()),
                    List.copyOf(middleContext.getRecipients())
            );

            EventUtil.callAsynchronously(messageEvent);

            if (middleContext.getChat().getSound() != null) {
                for (Player recipient : middleContext.getRecipients()) {
                    if (recipient != middleContext.getSender()) {
                        audiences.player(recipient).playSound(middleContext.getChat().getSound());
                    }
                }
            }

            if (middleContext.getChat().getRange() <= -3) {
                sendProxyMessage(middleContext);
            }

            List<MessageContext<Component>> groupedByStyle = groupedByStyle(middleContext, styles);
            for (int groupIndex = 0; groupIndex < groupedByStyle.size(); groupIndex++) {
                MessageContext<Component> groupContext = groupedByStyle.get(groupIndex);
                MessageContextKeys.putPlayers(groupContext, MessageContextKeys.ALL_RECIPIENTS,
                        middleContext.getRecipients());

                MessageContext<Component> lateContext = processor.handle(groupContext, Stage.LATE).getNewContext();
                delivered = true;
                sendProcessedMessage(lateContext, middleContext.getRecipients());

                // Format console message without style
                if (groupIndex == 0) {
                    String stringFormat = LegacyComponentSerializer.legacySection().serialize(lateContext.getFormat());
                    String stringMessage = LegacyComponentSerializer.legacySection().serialize(lateContext.getMessage());
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("{player}"), Matcher.quoteReplacement(lateContext.getSender().getDisplayName()));
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("{message}"), Matcher.quoteReplacement(stringMessage));
                    event.writeConsoleLine(stringFormat);
                    chatLogWriter.log(middleContext.getChat().getId(),
                            lateContext.getSender().getName(), stringMessage);
                    event.setMessage(stringMessage);
                }
            }

            if (middleContext.getChat().getRange() > -3) {
                sendNobodyHeardYou(event, middleContext);
            }

            long millisEnd = System.currentTimeMillis();
            long millisDelta = millisEnd - millisStart;

            if (settings.isDebug()) {
                logger.log(Level.INFO, "Later contexts processed for " + millisDelta + "ms (event: " + eventHashcode + ")");
            }

            processed = true;
        } catch (Throwable t) {
            handleProcessingFailure(t, event.getPlayer());
        } finally {
            if (!processed) {
                if (dropped || delivered) {
                    event.setCancelled(true);
                } else {
                    event.setRecipients(untouchedRecipients);
                }
            }
        }
    }

    /**
     * Records what the sender said and who they aimed it at, for
     * %chatty_player_message% and %chatty_targetname%.
     */
    private void rememberForPlaceholders(MessageContext<Component> context) {
        String message = PlainTextComponentSerializer.plainText().serialize(context.getMessage());
        Player sender = context.getSender();

        String targetName = sender.getName();
        for (Player candidate : context.getRecipients()) {
            if (candidate != sender && mentionsPlayer(message, candidate)) {
                targetName = candidate.getName();
                break;
            }
        }

        lastMessageState.remember(sender.getUniqueId(), message, targetName);
    }

    private boolean mentionsPlayer(String message, Player player) {
        String plainName = ChatColor.stripColor(player.getDisplayName());
        String pattern = settings.getMentions().getPattern()
                .replace("{username}", Pattern.quote(plainName));
        try {
            return Pattern.compile(pattern).matcher(message).find();
        } catch (Throwable t) {
            return false;
        }
    }

    private void sendProxyMessage(MessageContext<Component> middleContext) {
        Chat chat = middleContext.getChat();

        Component noStyleProxyMessage;
        Map<String, ru.brikster.chatty.proxy.data.ChatStyle> proxyStyles = new HashMap<>();

        MessageContext<Component> proxyNoStyleContext = new MessageContextImpl<>(middleContext);
        proxyNoStyleContext.setFormat(chat.getFormat(middleContext.getSender()));
        proxyNoStyleContext.setMessageFormat(chat.getMessageFormat(middleContext.getSender()));
        proxyNoStyleContext.setMessage(middleContext.getMessage());
        proxyNoStyleContext.setRecipients(Collections.emptyList());
        MessageContext<Component> proxyNoStyleLateContext = processor.handle(proxyNoStyleContext, Stage.LATE).getNewContext();
        noStyleProxyMessage = componentFromContextConstructor.construct(proxyNoStyleLateContext).compact();

        for (var style : chat.getStyles(middleContext.getSender())) {
            MessageContext<Component> proxyStyleContext = new MessageContextImpl<>(middleContext);
            proxyStyleContext.setFormat(style.format());
            proxyStyleContext.setMessageFormat(style.messageFormat());
            proxyStyleContext.setMessage(middleContext.getMessage());
            proxyStyleContext.setRecipients(Collections.emptyList());
            MessageContext<Component> proxyLateContext = processor.handle(proxyStyleContext, Stage.LATE).getNewContext();
            Component message = componentFromContextConstructor.construct(proxyLateContext).compact();

            proxyStyles.put(style.id(), new ru.brikster.chatty.proxy.data.ChatStyle(
                    style.priority(),
                    GsonComponentSerializer.gson().serialize(message),
                    proxyLateContext.getMessageFormat()
            ));
        }

        proxyService.sendChatMessage(chat, noStyleProxyMessage, proxyStyles, chat.getSound());
    }

    private void sendNobodyHeardYou(ChatEventFacade event, MessageContext<Component> middleContext) {
        if (middleContext.getChat().isSendNobodyHeardYou()) {
            Set<Player> allowedRecipients = new HashSet<>(middleContext.getRecipients());

            allowedRecipients.remove(event.getPlayer());

            if (settings.isHideVanishedRecipients()) {
                allowedRecipients.removeIf(player -> !event.getPlayer().canSee(player));
            }

            if (middleContext.getChat().isEnableSpy()) {
                allowedRecipients.removeAll(MessageContextKeys
                        .getPlayers(middleContext, MessageContextKeys.SPY_RECIPIENTS));
            }

            if (allowedRecipients.isEmpty()) {
                ChattyMessages.send(audiences.player(event.getPlayer()),
                        messages.getNobodyHeard());
            }
        }
    }

    private List<MessageContext<Component>> groupedByStyle(MessageContext<Component> context, Set<ChatStyle> styles) {
        Chat chat = context.getChat();
        boolean useSpy = chat.isEnableSpy()
                && MessageContextKeys.has(context, MessageContextKeys.SPY_RECIPIENTS);

        var grouping = chatStylePlayerGrouper.makeGrouping(context.getRecipients(), styles,
                useSpy ? MessageContextKeys.getPlayers(context, MessageContextKeys.SPY_RECIPIENTS) : null,
                useSpy ? new ChatStyle(
                        "internal-spy-style",
                        chat.getSpyFormat(),
                        chat.getMessageFormat(context.getSender()),
                        Integer.MAX_VALUE) : null);

        Map<ChatStyle, List<Player>> stylePlayersMap = grouping.getStylesMap();

        List<MessageContext<Component>> contexts = new ArrayList<>();

        List<Player> noStyleRecipients = grouping.getNoStylePlayers();

        MessageContext<Component> noStyleContext = new MessageContextImpl<>(context);
        noStyleContext.setMessage(context.getMessage());
        noStyleContext.setRecipients(noStyleRecipients);
        contexts.add(noStyleContext);

        stylePlayersMap.forEach((style, recipients) -> {
            MessageContext<Component> styleContext = new MessageContextImpl<>(context);
            styleContext.setFormat(style.format());
            styleContext.setMessageFormat(style.messageFormat());
            styleContext.setMessage(context.getMessage());
            styleContext.setRecipients(recipients);
            contexts.add(styleContext);
        });

        return contexts;
    }

    private void sendProcessedMessage(MessageContext<Component> lateContext,
                                      Collection<? extends @NotNull Player> middleContextRecipients) {
        Identity senderIdentity = Identity.identity(lateContext.getSender().getUniqueId());
        for (Player recipient : lateContext.getRecipients()) {
            MessageContext<Component> personalLateContext = new MessageContextImpl<>(lateContext);
            MessageContextKeys.putPlayers(personalLateContext, MessageContextKeys.ALL_RECIPIENTS,
                    middleContextRecipients);
            personalLateContext.setMessage(lateContext.getMessage());
            personalLateContext.setRecipients(Collections.singletonList(recipient));
            personalLateContext.setTarget(recipient);

            MessageContext<Component> postContext = processor.handle(personalLateContext, Stage.POST).getNewContext();

            Component message = componentFromContextConstructor.construct(postContext).compact();
            if (settings.isSendIdentifiedMessages()) {
                //noinspection deprecation
                audiences.player(recipient).sendMessage(senderIdentity, message);
            } else {
                audiences.player(recipient).sendMessage(message);
            }
        }
    }

}

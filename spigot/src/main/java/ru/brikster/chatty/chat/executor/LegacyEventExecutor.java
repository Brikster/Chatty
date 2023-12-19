package ru.brikster.chatty.chat.executor;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.ChatStyle;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.Stage;
import ru.brikster.chatty.api.event.ChattyMessageEvent;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructor;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.transform.intermediary.IntermediateMessageTransformer;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessor;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.SettingsConfig;

import javax.inject.Inject;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class LegacyEventExecutor implements Listener, EventExecutor {

    private final Map<Integer, MessageContext<String>> pendingMessages = new ConcurrentHashMap<>();

    @Inject private ChatSelector selector;
    @Inject private ComponentFromContextConstructor componentFromContextConstructor;
    @Inject private BukkitAudiences audiences;
    @Inject private SettingsConfig settings;
    @Inject private MessagesConfig messages;
    @Inject private MessageTransformStrategiesProcessor processor;
    @Inject private IntermediateMessageTransformer intermediateMessageTransformer;
    @Inject private Logger logger;

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
        Chat chat = selector.selectChat(event.getMessage(), chatCandidate ->
                !chatCandidate.isPermissionRequired() ||
                        chatCandidate.hasSymbolWritePermission(event.getPlayer()));

        if (chat == null) {
            audiences.player(event.getPlayer().getUniqueId())
                    .sendMessage(messages.getChatNotFound());
            event.setCancelled(true);
            return;
        }

        List<Player> recipients;
        if (settings.isRespectForeignRecipients()) {
            Predicate<Player> playerPredicate = chat.getRecipientPredicate(event.getPlayer());
            recipients = event.getRecipients().stream()
                    .filter(playerPredicate)
                    .collect(Collectors.toList());
        } else {
            recipients = new ArrayList<>(chat.getRecipients(event.getPlayer()));
        }

        MessageContext<String> context = new MessageContextImpl<>(
                chat,
                event.getPlayer(),
                new HashMap<>(),
                event.isCancelled(),
                chat.getFormat(),
                recipients,
                event.getMessage(),
                null);

        boolean processed = false;

        try {
            MessageContext<String> earlyContext = processor.handle(context, Stage.EARLY).getNewContext();

            event.getRecipients().clear();
            event.getRecipients().addAll(earlyContext.getRecipients());
            event.setMessage(earlyContext.getMessage());
            pendingMessages.put(System.identityHashCode(event), earlyContext);

            processed = true;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Cannot handle chat event", t);
        } finally {
            if (!processed) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleFinishedEarlyContextEvent(AsyncPlayerChatEvent event) {
        MessageContext<String> earlyContext = pendingMessages.remove(System.identityHashCode(event));
        if (earlyContext == null) {
            logger.log(Level.WARNING, "Cannot handle chat event from {0} with format {1} and message {2} due to hashcode idempotency error",
                    new Object[] { event.getPlayer().getName(), event.getFormat(), event.getMessage() });
            return;
        }

        if (earlyContext.isCancelled()) {
            event.setCancelled(true);
        }

        if (event.isCancelled()) return;

        earlyContext.setRecipients(new ArrayList<>(event.getRecipients()));
        earlyContext.setMessage(event.getMessage());

        event.getRecipients().clear();

        boolean processed = false;

        try {
            MessageContext<Component> earlyComponentContext = intermediateMessageTransformer.handle(earlyContext).getNewContext();
            MessageContext<Component> middleContext = processor.handle(earlyComponentContext, Stage.MIDDLE).getNewContext();

            ChattyMessageEvent messageEvent = new ChattyMessageEvent(
                    middleContext.getSender(),
                    middleContext.getChat(),
                    PlainTextComponentSerializer.plainText().serialize(middleContext.getMessage()),
                    List.copyOf(middleContext.getRecipients())
            );

            Bukkit.getPluginManager().callEvent(messageEvent);

            if (middleContext.getChat().getSound() != null) {
                for (Player recipient : middleContext.getRecipients()) {
                    if (recipient != middleContext.getSender()) {
                        audiences.player(recipient).playSound(middleContext.getChat().getSound());
                    }
                }
            }

            List<MessageContext<Component>> groupedByStyle = groupedByStyle(middleContext);
            for (int groupIndex = 0; groupIndex < groupedByStyle.size(); groupIndex++) {
                MessageContext<Component> groupContext = groupedByStyle.get(groupIndex);
                groupContext.getMetadata().put("all_recipients", middleContext.getRecipients());

                MessageContext<Component> lateContext = processor.handle(groupContext, Stage.LATE).getNewContext();
                sendProcessedMessage(lateContext, middleContext.getRecipients());

                // Format console message without style
                if (groupIndex == 0) {
                    String stringFormat = LegacyComponentSerializer.legacySection().serialize(lateContext.getFormat());
                    String stringMessage = LegacyComponentSerializer.legacySection().serialize(lateContext.getMessage());
                    stringFormat = stringFormat.replace("%", "%%");
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("{player}"), Matcher.quoteReplacement("%1$s"));
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("{message}"), Matcher.quoteReplacement("%2$s"));
                    event.setFormat(stringFormat);
                    event.setMessage(stringMessage);
                }
            }

            if (middleContext.getChat().isSendNobodyHeardYou()) {
                Set<Player> allowedRecipients = new HashSet<>();
                allowedRecipients.add(event.getPlayer());

                if (middleContext.getChat().isEnableSpy() && middleContext.getMetadata().containsKey("spy-recipients")) {
                    //noinspection unchecked
                    allowedRecipients.addAll((List<Player>) middleContext.getMetadata().get("spy-recipients"));
                }

                if (settings.isHideVanishedRecipients()) {
                    allowedRecipients.removeIf(player -> player != event.getPlayer()
                            && !event.getPlayer().canSee(player));
                }

                if (allowedRecipients.containsAll(middleContext.getRecipients())) {
                    audiences.player(event.getPlayer()).sendMessage(messages.getNobodyHeard());
                }
            }

            processed = true;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Cannot handle chat event", t);
        } finally {
            if (!processed) {
                event.setCancelled(true);
            }
        }
    }

    private List<MessageContext<Component>> groupedByStyle(MessageContext<Component> context) {
        Map<Player, ChatStyle> playerStyleMap = new HashMap<>();

        for (ChatStyle style : context.getChat().getStyles()) {
            for (Player recipient : context.getRecipients()) {
                ChatStyle currentStyle = playerStyleMap.get(recipient);
                if (currentStyle == null || style.priority() > currentStyle.priority()) {
                    if (recipient.hasPermission("chatty.style." + style.id())) {
                        playerStyleMap.put(recipient, style);
                    }
                }
            }
        }

        if (context.getChat().isEnableSpy() && context.getMetadata().containsKey("spy-recipients")) {
            @SuppressWarnings("unchecked")
            List<Player> players = (List<Player>) context.getMetadata().get("spy-recipients");

            ChatStyle spyStyle = new ChatStyle(
                    null,
                    context.getChat().getSpyFormat(),
                    Integer.MAX_VALUE);

            players.forEach(spyPlayer -> playerStyleMap.put(spyPlayer, spyStyle));
        }

        Map<ChatStyle, List<Player>> stylePlayersMap = new HashMap<>();

        for (Entry<Player, ChatStyle> entry : playerStyleMap.entrySet()) {
            stylePlayersMap.compute(entry.getValue(), (k, v) -> {
                List<Player> players = v;
                if (players == null) {
                    players = new ArrayList<>();
                }
                players.add(entry.getKey());
                return players;
            });
        }

        List<MessageContext<Component>> contexts = new ArrayList<>();

        List<Player> noStyleRecipients = new ArrayList<>();
        for (Player recipient : context.getRecipients()) {
            if (!playerStyleMap.containsKey(recipient)) {
                noStyleRecipients.add(recipient);
            }
        }

        MessageContext<Component> noStyleContext = new MessageContextImpl<>(context);
        noStyleContext.setMessage(context.getMessage());
        noStyleContext.setRecipients(noStyleRecipients);
        contexts.add(noStyleContext);

        stylePlayersMap.forEach((style, recipients) -> {
            MessageContext<Component> styleContext = new MessageContextImpl<>(context);
            styleContext.setFormat(style.format());
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
            personalLateContext.getMetadata().put("all_recipients", middleContextRecipients);
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

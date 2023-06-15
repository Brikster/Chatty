package ru.brikster.chatty.chat.executor;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy.Stage;
import ru.brikster.chatty.chat.construct.ComponentFromContextConstructor;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.transform.processor.MessageTransformStrategiesProcessor;
import ru.brikster.chatty.chat.message.transform.stage.intermediary.IntermediateMessageTransformer;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.SettingsConfig;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyEventExecutor implements Listener, EventExecutor {

    private final Deque<MessageContext<String>> pendingMessages = new ArrayDeque<>();

    @Inject private ChatSelector selector;
    @Inject private ComponentFromContextConstructor componentFromContextConstructor;
    @Inject private BukkitAudiences audiences;
    @Inject private SettingsConfig settings;
    @Inject private MessagesConfig messages;
    @Inject private MessageTransformStrategiesProcessor processor;
    @Inject private IntermediateMessageTransformer intermediateMessageTransformer;

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
        Chat chat = selector.selectChat(event.getMessage(), $ ->
                !$.isPermissionRequired() ||
                        $.hasSymbolWritePermission(event.getPlayer()));

        if (chat == null) {
            audiences.player(event.getPlayer().getUniqueId())
                    .sendMessage(messages.getChatNotFound());
            event.setCancelled(true);
            return;
        }

        MessageContext<String> context = new MessageContextImpl<>(
                chat,
                event.getPlayer(),
                event.isCancelled(),
                chat.getFormat(),
                new ArrayList<>(settings.isRespectForeignRecipients()
                        ? event.getRecipients()
                        : Bukkit.getOnlinePlayers()),
                event.getMessage(),
                null);

        MessageContext<String> newContext = processor.handle(context, Stage.EARLY).getNewContext();

        if (newContext.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.getRecipients().clear();
            event.getRecipients().addAll(newContext.getRecipients());
            event.setMessage(newContext.getMessage());
            pendingMessages.add(newContext);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleFinishedEvent(AsyncPlayerChatEvent event) {
        if (!pendingMessages.isEmpty()) {
            MessageContext<String> context = pendingMessages.pop();

            if (event.isCancelled()) return;

            context.setRecipients(event.getRecipients());
            context.setMessage(event.getMessage());

            for (MessageContext<String> groupContext : groupedByRecipients(context)) {
                MessageContext<String> lateContext = processor.handle(groupContext, Stage.LATE).getNewContext();

                if (!lateContext.isCancelled()) {
                    MessageContext<Component> componentContext = intermediateMessageTransformer.handle(lateContext).getNewContext();

                    event.getRecipients().clear();

                    String stringFormat = LegacyComponentSerializer.legacySection().serialize(componentContext.getFormat());
                    String stringMessage = LegacyComponentSerializer.legacySection().serialize(componentContext.getMessage());
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("{player}"), Matcher.quoteReplacement("%1$s"));
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("{message}"), Matcher.quoteReplacement("%2$s"));
                    event.setFormat(stringFormat);
                    event.setMessage(stringMessage);

                    sendProcessedMessage(componentContext);
                }
            }
        }
    }

    private Set<MessageContext<String>> groupedByRecipients(MessageContext<String> context) {
        return new HashSet<>(Arrays.asList(context));
    }

    private void sendProcessedMessage(MessageContext<Component> context) {
        Identity senderIdentity = Identity.identity(context.getSender().getUniqueId());
        for (Player recipient : context.getRecipients()) {
            MessageContext<Component> recipientContext = new MessageContextImpl<>(context);
            recipientContext.setMessage(context.getMessage());
            recipientContext.setTarget(recipient);

            MessageContext<Component> newContext = processor.handle(recipientContext, Stage.POST).getNewContext();

            Component message = componentFromContextConstructor.construct(newContext).compact();
            audiences.player(recipient).sendMessage(senderIdentity, message);
        }
    }

}

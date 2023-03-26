package ru.brikster.chatty.chat.executor;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.chat.construct.MessageConstructor;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.general.EarlyMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.general.LateMessageTransformStrategy;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.SettingsConfig;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.regex.Pattern;

public class LegacyEventExecutor implements Listener, EventExecutor {

    private final Deque<MessageContext<String>> pendingMessages = new ArrayDeque<>();

    @Inject private ChatSelector selector;
    @Inject private MessageConstructor messageConstructor;
    @Inject private BukkitAudiences audiences;
    @Inject private SettingsConfig settings;
    @Inject private MessagesConfig messages;

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
                new ArrayList<>(event.getRecipients()),
                event.getMessage()
        );

        EarlyMessageTransformStrategy strategy = new EarlyMessageTransformStrategy();
        MessageContext<String> newContext = strategy.handle(context).getNewContext();

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
            context.setRecipients(event.getRecipients());
            context.setMessage(event.getMessage());

            LateMessageTransformStrategy strategy = new LateMessageTransformStrategy();
            MessageContext<Component> newContext = strategy.handle(context).getNewContext();

            if (!newContext.isCancelled() && !event.isCancelled()) {
                if (settings.isForceStringFormatIfLegacyMethod()) {
                    String stringFormat = LegacyComponentSerializer.legacy('§').serialize(newContext.getFormat());
                    String stringMessage  = LegacyComponentSerializer.legacy('§').serialize(newContext.getMessage());
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("<player>"), "%1$s");
                    stringFormat = stringFormat.replaceFirst(Pattern.quote("<message>"), "%2$s");
                    event.setFormat(stringFormat);
                    event.setMessage(stringMessage);
                } else {
                    Component message = messageConstructor.construct(newContext).compact();

                    Audience.audience(
                            audiences.filter(sender -> sender instanceof Player && newContext.getRecipients().contains(sender)),
                            audiences.console()
                    ).sendMessage(Identity.identity(event.getPlayer().getUniqueId()), message);

                    event.setCancelled(true);
                }
            }
        }
    }

}

package ru.brikster.chatty.chat.executor;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.chat.construct.MessageConstructor;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.general.EarlyMessageTransformStrategy;
import ru.brikster.chatty.chat.handle.strategy.general.LateMessageTransformStrategy;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.config.Configs;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class LegacyEventExecutor implements Listener, EventExecutor {

    private final Deque<MessageContext<String>> pendingMessages = new ArrayDeque<>();
    @Inject
    private ChatSelector selector;
    @Inject
    private MessageConstructor messageConstructor;

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
                !$.isPermissionRequired()
                        || $.hasSymbolWritePermission(event.getPlayer()));

        if (chat == null) {
            BukkitAudiences.create(Chatty.get())
                    .player(event.getPlayer())
                    .sendMessage(Configs.MESSAGES.getChatNotFound());
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
                Component message = messageConstructor.construct(newContext).compact();

                BukkitAudiences.create(Chatty.get())
                        .filter(sender -> sender instanceof Player
                                && newContext.getRecipients().contains(sender))
                        .sendMessage(message);

                BukkitAudiences.create(Chatty.get())
                        .sender(Bukkit.getConsoleSender())
                        .sendMessage(message);

                event.setCancelled(true);
            }
        }
    }

}

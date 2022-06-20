package ru.brikster.chatty.chat.executor;

import javax.inject.Inject;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.construct.MessageConstructor;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.impl.GeneralMessageHandleStrategy;
import ru.brikster.chatty.chat.handle.strategy.impl.SimpleComponentStrategy;
import ru.brikster.chatty.convert.component.ComponentConverter;

public class LegacyEventExecutor implements Listener, EventExecutor {

    @Inject
    private ComponentConverter converter;
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
        Component format = converter.convert("&e<b><hover:show_text:'test'>{player}</hover>&8: <white>{message}");

        Chat chat = new ChatImpl(
                "test", "Тест",
                format, "", null,
                -3, false
        );

        chat.addStrategy(new SimpleComponentStrategy());

        MessageContext<String> context = new MessageContextImpl<>(
                event.isCancelled(),
                format,
                event.getRecipients(),
                event.getMessage(),
                chat,
                event.getPlayer()
        );

        GeneralMessageHandleStrategy strategy = new GeneralMessageHandleStrategy();
        MessageContext<Component> newContext = strategy.handle(context).getNewContext();

        event.setCancelled(true);

        Component message = messageConstructor.construct(newContext).compact();
;

        System.out.println(GsonComponentSerializer.gson().serialize(message));

        BukkitAudiences.create(Chatty.get())
                .filter(sender -> sender instanceof Player
                        && newContext.getRecipients().contains(sender))
                .sendMessage(message);

        BukkitAudiences.create(Chatty.get())
                .sender(Bukkit.getConsoleSender())
                .sendMessage(message);
    }

}

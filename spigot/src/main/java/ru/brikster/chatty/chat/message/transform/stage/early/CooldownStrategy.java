package ru.brikster.chatty.chat.message.transform.stage.early;

import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Singleton
public final class CooldownStrategy implements MessageTransformStrategy<String> {

    @Inject private MessagesConfig messagesConfig;
    @Inject private BukkitAudiences audiences;

    private final ConcurrentMap<String, ChatCooldowns> cooldownsByChat = new ConcurrentHashMap<>();

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        Chat chat = context.getChat();
        if (chat.getCooldown() <= 0) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        ChatCooldowns cooldowns = cooldownsByChat.computeIfAbsent(chat.getId(), id -> new ChatCooldowns(id, chat.getCooldown()));
        Player sender = context.getSender();
        if (sender.hasPermission("chatty.bypass.cooldown") || sender.hasPermission(cooldowns.bypassPermission)) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        // Keyed by UUID so the cooldown survives a rejoin, entries expire on their own once it ends
        long now = System.currentTimeMillis();
        Long previousMillis = cooldowns.lastMessageMillis.putIfAbsent(sender.getUniqueId(), now);
        if (previousMillis == null) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        long millisLeft = previousMillis + cooldowns.cooldownMillis - now;
        long secondsLeft = Math.max(1, (millisLeft + 999) / 1000);
        audiences.player(sender)
                .sendMessage(messagesConfig.getWaitCooldown()
                        .replaceText(AdventureUtil.createReplacement("{secondsLeft}", secondsLeft)));
        return MessageTransformResultBuilder.<String>fromContext(context)
                .withCancelled()
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    private static final class ChatCooldowns {

        private final String bypassPermission;
        private final long cooldownMillis;
        private final ConcurrentMap<UUID, Long> lastMessageMillis;

        private ChatCooldowns(String chatId, int cooldownSeconds) {
            this.bypassPermission = "chatty.bypass.cooldown." + chatId;
            this.cooldownMillis = cooldownSeconds * 1000L;
            this.lastMessageMillis = CacheBuilder.newBuilder()
                    .expireAfterWrite(cooldownMillis, TimeUnit.MILLISECONDS)
                    .<UUID, Long>build()
                    .asMap();
        }

    }

}

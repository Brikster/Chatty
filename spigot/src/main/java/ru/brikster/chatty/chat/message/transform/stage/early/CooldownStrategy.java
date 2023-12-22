package ru.brikster.chatty.chat.message.transform.stage.early;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class CooldownStrategy implements MessageTransformStrategy<String> {

    @Inject private MessagesConfig messagesConfig;
    @Inject private BukkitAudiences audiences;

    private final ConcurrentHashMap<String, Cache<CommandSender, Long>> cooldownCachesMap = new ConcurrentHashMap<>();

    @SneakyThrows
    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        String chatName = context.getChat().getId();

        if (context.getChat().getCooldown() > 0
                && !context.getSender().hasPermission("chatty.cooldown." + chatName)) {
            Cache<CommandSender, Long> cache = cooldownCachesMap.computeIfAbsent(chatName, (k) -> CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .weakKeys()
                    .build());

            Long previousMillis = cache.getIfPresent(context.getSender());
            if (previousMillis != null) {
                long endCooldownMillis = previousMillis + context.getChat().getCooldown() * 1000L;
                if (System.currentTimeMillis() < endCooldownMillis) {
                    long secondsLeft = (long) Math.ceil((double) (endCooldownMillis - System.currentTimeMillis()) / 1000d);
                    audiences.sender(context.getSender())
                            .sendMessage(messagesConfig.getWaitCooldown()
                                    .replaceText(AdventureUtil.createReplacement("{secondsLeft}", secondsLeft)));
                    return MessageTransformResultBuilder.<String>fromContext(context)
                            .withCancelled()
                            .build();
                }
            }

            cache.put(context.getSender(), System.currentTimeMillis());
        }
        return MessageTransformResultBuilder.<String>fromContext(context).build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

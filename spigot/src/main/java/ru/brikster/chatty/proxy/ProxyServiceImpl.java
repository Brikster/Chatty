package ru.brikster.chatty.proxy;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.chat.ChatStyle;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.style.ChatStylePlayerGrouper;
import ru.brikster.chatty.chat.style.ChatStylePlayerGrouper.Groping;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.proxy.data.ChatMessage;
import ru.brikster.chatty.proxy.data.PrivateMessage;
import ru.brikster.chatty.proxy.data.ProxyPlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public final class ProxyServiceImpl implements ProxyService {

    private static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();

    private final UUID clientId = UUID.randomUUID();

    private final RMapCache<String, ProxyPlayer> playersCache;
    private final RMapCache<String, String> pmReplyCache;
    private final RTopic chatTopic;
    private final RTopic pmTopic;
    private final ScheduledExecutorService scheduledExecutor;

    @Inject
    public ProxyServiceImpl(Config redissonConfig,
                            BukkitAudiences audiences,
                            ChatRegistry chatRegistry,
                            PmConfig pmConfig,
                            ChatStylePlayerGrouper stylePlayerGrouper,
                            Plugin plugin) {
        RedissonClient redissonClient = Redisson.create(redissonConfig);
        this.playersCache = redissonClient.getMapCache("chatty_players");
        this.pmReplyCache = redissonClient.getMapCache("chatty_pm_reply");
        this.chatTopic = redissonClient.getTopic("chatty_chat");
        this.pmTopic = redissonClient.getTopic("chatty_pm");

        chatTopic.addListener(ChatMessage.class, (channel, redisMessage) -> {
            if (redisMessage.getClientId().equals(clientId)) return;

            Chat chat = chatRegistry.getChats().get(redisMessage.getChatId());
            if (chat.getRange() > -3) return; // not cross-proxy chat

            Component noStyleComponent = GSON_COMPONENT_SERIALIZER.deserialize(redisMessage.getNoStyleComponentJson());

            var recipients = chat.calculateRecipients(null);

            Set<ChatStyle> styles = redisMessage.getStyleComponentJsonMap()
                    .entrySet()
                    .stream()
                    .map(entry -> new ChatStyle(entry.getKey(),
                            GSON_COMPONENT_SERIALIZER.deserialize(entry.getValue().getComponentJson()),
                            entry.getValue().getPriority()))
                    .collect(Collectors.toSet());

            Groping grouping = stylePlayerGrouper.makeGrouping(recipients, styles, null, null);

            Sound sound = redisMessage.getSound();

            for (Player noStylePlayer : grouping.getNoStylePlayers()) {
                var playerAudience = audiences.player(noStylePlayer);
                playerAudience.sendMessage(noStyleComponent);
                if (sound != null) {
                    playerAudience.playSound(sound);
                }
            }

            grouping.getStylesMap().forEach((style, players) -> {
                for (Player player : players) {
                    var playerAudience = audiences.player(player);
                    playerAudience.sendMessage(style.format());
                    if (sound != null) {
                        playerAudience.playSound(sound);
                    }
                }
            });

            audiences.console().sendMessage(noStyleComponent);
        });

        if (pmConfig.isEnable()) {
            pmTopic.addListener(PrivateMessage.class, (channel, redisMessage) -> {
                if (redisMessage.getClientId().equals(clientId)) return;

                if (redisMessage.getSpyComponentJson() != null) {
                    Component spyMessage = GSON_COMPONENT_SERIALIZER.deserialize(redisMessage.getSpyComponentJson());
                    audiences.filter(spyCandidate ->
                                    spyCandidate.hasPermission("chatty.spy.pm")
                                            && !(spyCandidate instanceof ConsoleCommandSender)
                                            && !spyCandidate.getName().equalsIgnoreCase(redisMessage.getTargetName()))
                            .sendMessage(spyMessage);
                }

                Player targetPlayer = Bukkit.getPlayerExact(redisMessage.getTargetName());
                if (targetPlayer == null) return;

                Component message = GSON_COMPONENT_SERIALIZER.deserialize(redisMessage.getComponentJson());

                var targetPlayerAudience = audiences.player(targetPlayer);

                targetPlayerAudience.sendMessage(message);
                if (pmConfig.isPlaySound()) {
                    targetPlayerAudience.playSound(pmConfig.getSound());
                }

                plugin.getLogger().info(redisMessage.getLogMessage());
            });
        }

        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutor.scheduleWithFixedDelay(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playersCache.put(player.getName().toLowerCase(), new ProxyPlayer(player.getName(), player.getUniqueId()), 8, TimeUnit.SECONDS);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public @NotNull Collection<String> getOnlinePlayers() {
        return playersCache
                .values()
                .stream()
                .map(ProxyPlayer::getUsername)
                .collect(Collectors.toSet());
    }

    @Override
    public @Nullable UUID getUuidByUsername(@NotNull String username) {
        ProxyPlayer player = playersCache.get(username.toLowerCase());
        if (player != null) return player.getUuid();
        return null;
    }

    @Override
    public void addConversation(@NotNull String firstSender, @NotNull String secondSender) {
        pmReplyCache.put(firstSender.toLowerCase(), secondSender.toLowerCase(), 10, TimeUnit.MINUTES);
    }

    @Override
    public @Nullable String getLastConversation(@NotNull String sender) {
        String playerName = pmReplyCache.get(sender.toLowerCase());

        if (playerName == null) return null;
        ProxyPlayer proxyPlayer = playersCache.get(playerName);

        if (proxyPlayer == null) return null;
        return proxyPlayer.getUsername();
    }

    @Override
    public boolean isOnline(@NotNull String playerName) {
        return playersCache.containsKey(playerName.toLowerCase());
    }

    @Override
    public void sendChatMessage(@NotNull Chat chat,
                                @NotNull Component noStyleMessage,
                                @NotNull Map<String, ru.brikster.chatty.proxy.data.ChatStyle> stylesMessages,
                                @Nullable Sound sound) {
        chatTopic.publish(new ChatMessage(clientId, chat.getId(),
                GSON_COMPONENT_SERIALIZER.serialize(noStyleMessage),
                stylesMessages,
                sound));
    }

    @Override
    public void sendPrivateMessage(@NotNull String targetName,
                                   @NotNull Component message,
                                   @Nullable Component spyMessage,
                                   @NotNull String logMessage,
                                   @Nullable Sound sound) {
        pmTopic.publish(new PrivateMessage(clientId,
                targetName,
                GSON_COMPONENT_SERIALIZER.serialize(message),
                spyMessage == null ? null : GSON_COMPONENT_SERIALIZER.serialize(spyMessage),
                logMessage,
                sound));
    }

    @Override
    public void close() {
        this.scheduledExecutor.shutdown();
        this.chatTopic.removeAllListeners();
        this.pmTopic.removeAllListeners();
    }

}

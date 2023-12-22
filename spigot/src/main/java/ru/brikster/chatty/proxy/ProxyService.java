package ru.brikster.chatty.proxy;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.proxy.data.ChatStyle;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ProxyService {

    @NotNull Collection<String> getOnlinePlayers();

    @Nullable UUID getUuidByUsername(@NotNull String username);

    void addConversation(@NotNull String firstSender, @NotNull String secondSender);

    @Nullable String getLastConversation(@NotNull String sender);

    default boolean isOnline(@NotNull String playerName) {
        return getOnlinePlayers()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(playerName.toLowerCase());
    }

    void sendChatMessage(@NotNull Chat chat,
                         @NotNull Component noStyleMessage,
                         @NotNull Map<String, ChatStyle> stylesMessages,
                         @Nullable Sound sound);

    void sendPrivateMessage(@NotNull String targetName,
                            @NotNull Component message,
                            @Nullable Component spyMessage,
                            @NotNull String logMessage,
                            @Nullable Sound sound);

}

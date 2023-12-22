package ru.brikster.chatty.proxy;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.proxy.data.ChatStyle;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Singleton
public final class DummyProxyService implements ProxyService {

    @Override
    public @NotNull Collection<String> getOnlinePlayers() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable UUID getUuidByUsername(@NotNull String username) {
        return null;
    }

    @Override
    public void addConversation(@NotNull String firstSender, @NotNull String secondSender) {

    }

    @Override
    public @Nullable String getLastConversation(@NotNull String sender) {
        return null;
    }

    @Override
    public void sendChatMessage(@NotNull Chat chat, @NotNull Component noStyleMessage, @NotNull Map<String, ChatStyle> stylesMessages, @Nullable Sound sound) {

    }

    @Override
    public void sendPrivateMessage(@NotNull String targetName, @NotNull Component message, @Nullable Component spyMessage, @NotNull String logMessage, @Nullable Sound sound) {

    }

}

package ru.brikster.chatty.chat.selection;

import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class ChatSelectionState {

    private final Map<UUID, String> switchedChats = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingChats = new ConcurrentHashMap<>();

    public void switchTo(UUID playerUuid, String chatId) {
        switchedChats.put(playerUuid, chatId);
    }

    public void clearSwitch(UUID playerUuid) {
        switchedChats.remove(playerUuid);
    }

    public @Nullable String getSwitchedChat(UUID playerUuid) {
        return switchedChats.get(playerUuid);
    }

    public boolean isSwitchedTo(UUID playerUuid, String chatId) {
        return chatId.equals(switchedChats.get(playerUuid));
    }

    public void sendNext(UUID playerUuid, String chatId) {
        pendingChats.put(playerUuid, chatId);
    }

    public @Nullable String takePendingChat(UUID playerUuid) {
        return pendingChats.remove(playerUuid);
    }

    public void forget(UUID playerUuid) {
        switchedChats.remove(playerUuid);
        pendingChats.remove(playerUuid);
    }

}

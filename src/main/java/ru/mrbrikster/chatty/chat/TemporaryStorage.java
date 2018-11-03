package ru.mrbrikster.chatty.chat;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryStorage {

    @Getter private final List<Player> spyDisabled = new ArrayList<>();
    private Map<String, String> messagesStorage;

    public TemporaryStorage() {
        this.messagesStorage = new ConcurrentHashMap<>();
    }

    public void setLastMessaged(String sender, String recipient) {
        this.messagesStorage.put(sender, recipient);
    }

    public Optional<String> getLastMessaged(String sender) {
        if (messagesStorage.containsKey(sender)) {
            return Optional.of(messagesStorage.get(sender));
        } else return Optional.empty();
    }

}

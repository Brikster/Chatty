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
    private Map<Player, Player> messagesStorage;

    public TemporaryStorage() {
        this.messagesStorage = new ConcurrentHashMap<>();
    }

    public void setLastMessaged(Player player, Player lastMessaged) {
        this.messagesStorage.put(player, lastMessaged);
    }

    public Optional<Player> getLastMessaged(Player player) {
        if (messagesStorage.containsKey(player)) {
            return Optional.of(messagesStorage.get(player));
        } else return Optional.empty();
    }

}

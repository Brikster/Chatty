package ru.mrbrikster.chatty.commands.pm;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MessagesStorage {

    private Map<Player, Player> messagesStorage;

    public MessagesStorage() {
        this.messagesStorage = new ConcurrentHashMap<>();
    }

    void setLastMessaged(Player player, Player lastMessaged) {
        this.messagesStorage.put(player, lastMessaged);
    }

    Optional<Player> getLastMessaged(Player player) {
        if (messagesStorage.containsKey(player)) {
            return Optional.of(messagesStorage.get(player));
        } else return Optional.empty();
    }

}

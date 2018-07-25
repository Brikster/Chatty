package ru.mrbrikster.chatty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyPool;

public class LOW extends ChatListener {

    public LOW(Configuration configuration,
                ChatManager chatManager,
                DependencyPool dependencyPool) {
        super(configuration,
                chatManager,
                dependencyPool);
    }

    @EventHandler(
            priority = EventPriority.LOW,
            ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        super.onChat(playerChatEvent);
    }

}

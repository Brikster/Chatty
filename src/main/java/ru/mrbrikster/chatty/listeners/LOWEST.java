package ru.mrbrikster.chatty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.dependencies.DependencyPool;
import ru.mrbrikster.chatty.moderation.ModerationManager;

public class LOWEST extends ChatListener {

    public LOWEST(Configuration configuration,
                  ChatManager chatManager,
                  DependencyPool dependencyPool,
                  ModerationManager moderationManager) {
        super(configuration,
                chatManager,
                dependencyPool,
                moderationManager);
    }

    @EventHandler(
            priority = EventPriority.LOWEST,
            ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        super.onChat(playerChatEvent);
    }

}

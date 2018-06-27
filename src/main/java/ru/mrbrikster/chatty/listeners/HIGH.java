package ru.mrbrikster.chatty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.managers.EventManager;

public class HIGH extends EventManager {

    public HIGH(Main main) {
        super(main);
    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        super.onChat(playerChatEvent);
    }

}

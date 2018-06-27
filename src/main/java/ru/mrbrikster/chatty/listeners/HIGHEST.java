package ru.mrbrikster.chatty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.managers.EventManager;

public class HIGHEST extends EventManager {

    public HIGHEST(Main main) {
        super(main);
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        super.onChat(playerChatEvent);
    }

}

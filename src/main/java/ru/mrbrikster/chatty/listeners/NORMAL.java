package ru.mrbrikster.chatty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.Main;
import ru.mrbrikster.chatty.managers.EventManager;

public class NORMAL extends EventManager {

    public NORMAL(Main main) {
        super(main);
    }

    @EventHandler(
            priority = EventPriority.NORMAL,
            ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent playerChatEvent) {
        super.onChat(playerChatEvent);
    }

}

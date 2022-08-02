package ru.brikster.chatty.api.event;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChattyInitEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private BukkitAudiences audienceProvider;

    public ChattyInitEvent(BukkitAudiences audienceProvider) {
        this.audienceProvider = audienceProvider;
    }

    /**
     * Get AudienceProvider that Chatty will use
     * @return audience provider
     */
    public BukkitAudiences getAudienceProvider() {
        return audienceProvider;
    }

    /**
     * Set custom BukkitAudiences. May be useful to write addons for modded servers support
     * @param audienceProvider custom audience provider
     */
    public void setAudienceProvider(final BukkitAudiences audienceProvider) {
        this.audienceProvider = audienceProvider;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}

package ru.brikster.chatty.chat.executor;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public interface ChatEventFacade {

    Player getPlayer();

    String getMessage();

    void setMessage(String message);

    List<Player> getRecipients();

    void setRecipients(Collection<? extends Player> recipients);

    void clearRecipients();

    boolean isCancelled();

    void setCancelled(boolean cancelled);

    void writeConsoleLine(String line);

    String getDebugFormat();

}

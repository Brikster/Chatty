package ru.brikster.chatty.chat.executor;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
final class LegacyChatEventFacade implements ChatEventFacade {

    private final AsyncPlayerChatEvent event;

    @Override
    public Player getPlayer() {
        return event.getPlayer();
    }

    @Override
    public String getMessage() {
        return event.getMessage();
    }

    @Override
    public void setMessage(String message) {
        event.setMessage(message);
    }

    @Override
    public List<Player> getRecipients() {
        return new ArrayList<>(event.getRecipients());
    }

    @Override
    public void setRecipients(Collection<? extends Player> recipients) {
        event.getRecipients().clear();
        event.getRecipients().addAll(recipients);
    }

    @Override
    public void clearRecipients() {
        event.getRecipients().clear();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }

    @Override
    public void writeConsoleLine(String line) {
        event.setFormat(line.replace("%", "%%"));
    }

    @Override
    public String getDebugFormat() {
        return event.getFormat();
    }

}

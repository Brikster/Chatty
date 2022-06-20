package ru.brikster.chatty.api.chat.handle.context;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.Chat;

import java.util.Collection;

public interface MessageContext<T> {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

    @NotNull
    Component getFormat();

    void setFormat(@NotNull Component component);

    @NotNull
    Collection<? extends @NotNull Player> getRecipients();

    void setRecipients(@NotNull Collection<? extends @NotNull Player> recipients);

    @NotNull
    T getMessage();

    void setMessage(@NotNull T message);

    @NotNull
    Chat getChat();

    @NotNull
    Player getSender();

}

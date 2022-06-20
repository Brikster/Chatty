package ru.brikster.chatty.chat.handle.strategy.result;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageHandleStrategy.Result;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.Collections;

@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class ResultImpl<T> implements Result<T> {

    @NotNull MessageContext<T> newContext;

    @Default
    Collection<? extends @NotNull Player> addedRecipients = Collections.emptyList();

    @Default
    Collection<? extends @NotNull Player> removedRecipients = Collections.emptyList();

    boolean formatUpdated;
    boolean messageUpdated;

    boolean becameCancelled;
    boolean becameUncancelled;

}

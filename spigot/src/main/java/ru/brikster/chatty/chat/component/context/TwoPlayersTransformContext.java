package ru.brikster.chatty.chat.component.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor(staticName = "of")
public final class TwoPlayersTransformContext implements TransformContext {

    private final @Getter Player one;
    private final @Getter Player two;

}

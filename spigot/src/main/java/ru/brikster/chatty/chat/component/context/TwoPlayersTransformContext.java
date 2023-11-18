package ru.brikster.chatty.chat.component.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class TwoPlayersTransformContext implements TransformContext {

    private final Player one;
    private final Player two;

}

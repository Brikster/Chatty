package ru.brikster.chatty.chat.component.context;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class SinglePlayerTransformContext implements TransformContext {

    private final @Getter Player player;

}

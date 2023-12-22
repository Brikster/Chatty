package ru.brikster.chatty.chat.component.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;

@Getter
@RequiredArgsConstructor(staticName = "of")
public final class SinglePlayerTransformContext implements TransformContext {

    private final OfflinePlayer player;

}

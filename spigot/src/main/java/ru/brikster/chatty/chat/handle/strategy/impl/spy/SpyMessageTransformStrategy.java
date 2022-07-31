package ru.brikster.chatty.chat.handle.strategy.impl.spy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.permission.spy.SpyPermission;

import java.util.ArrayList;
import java.util.List;

public class SpyMessageTransformStrategy implements MessageTransformStrategy {

    @Override
    public @NotNull Result handle(MessageContext context) {


        return null;
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    private List<Player> collectSpies(String chatName) {
        List<Player> spies = new ArrayList<>();

        String generalSpyPermission = new SpyPermission().build();
        String chatSpyPermission = new SpyPermission().withChat(chatName);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(generalSpyPermission)
                    || player.hasPermission(chatSpyPermission)) {
                spies.add(player);
            }
        }

        return spies;
    }

}

package ru.mrbrikster.chatty.bungee;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;

public class BungeeBroadcaster {

    @SuppressWarnings("all")
    public static void broadcast(String chat, String message, boolean json) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("chatty");
        out.writeUTF(chat);
        out.writeUTF(message);
        out.writeBoolean(json);

        Player player = Iterables.getFirst(Reflection.getOnlinePlayers(), null);

        if (player != null) {
            player.sendPluginMessage(Chatty.instance(), "BungeeCord", out.toByteArray());
        }
    }

}

package ru.mrbrikster.chatty.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;
import ru.mrbrikster.chatty.reflection.Reflection;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeCordListener implements PluginMessageListener {

    public final static UUID SERVER_UUID = UUID.randomUUID();

    private final ChatManager chatManager;

    public BungeeCordListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @SuppressWarnings("all")
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("chatty")) {
            short length = in.readShort();
            byte[] bytes = new byte[length];
            in.readFully(bytes);

            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytes));

            String chatName;
            String text;
            boolean json;

            try {
                chatName = inputStream.readUTF();

                UUID uuid = UUID.fromString(inputStream.readUTF());
                if (uuid.equals(SERVER_UUID)) return;

                text = inputStream.readUTF();
                json = inputStream.readBoolean();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Optional<Chat> optionalChat = chatManager.getChats().stream().filter(c -> c.getName().equals(chatName)).findAny();

            if (!optionalChat.isPresent()) {
                return;
            }

            Chat chat = optionalChat.get();

            if (chat.getRange() > -3) {
                return;
            }

            if (json) {
                FancyMessage fancyMessage = FancyMessage.deserialize(text);
                fancyMessage.send(Reflection.getOnlinePlayers().stream().filter(recipient -> {
                    return !chat.isPermissionRequired()
                            || recipient.hasPermission("chatty.chat." + chat.getName() + ".see")
                            || recipient.hasPermission("chatty.chat." + chat.getName());
                }).collect(Collectors.toList()));

                fancyMessage.send(Bukkit.getConsoleSender());
            } else {
                Reflection.getOnlinePlayers().stream().filter(recipient -> {
                    return !chat.isPermissionRequired()
                            || recipient.hasPermission("chatty.chat." + chat.getName() + ".see")
                            || recipient.hasPermission("chatty.chat." + chat.getName());
                }).forEach(onlinePlayer -> {
                    onlinePlayer.sendMessage(text);
                });

                Bukkit.getConsoleSender().sendMessage(text);
            }
        }
    }

}

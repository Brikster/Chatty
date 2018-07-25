package ru.mrbrikster.chatty.chat;

import lombok.Getter;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.config.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {

    @Getter private final List<Chat> chats = new ArrayList<>();

    public ChatManager(Configuration configuration) {
        for (String chatName : configuration.getNode("chats")
                .getAsConfigurationSection().getKeys(false)) {
            ConfigurationNode chatNode = configuration.getNode(String.format("chats.%s", chatName));
            this.chats.add(new Chat(chatName,
                    chatNode.getNode("enable").getAsBoolean(false),
                    chatNode.getNode("format").getAsString("{prefix}{player}{suffix}: {message}"),
                    chatNode.getNode("range").getAsInt(-1),
                    chatNode.getNode("symbol").getAsString(""),
                    chatNode.getNode("permission").getAsBoolean(true),
                    chatNode.getNode("cooldown").getAsLong(-1),
                    chatNode.getNode("money").getAsInt(0)));
        }
    }

    public void clear() {
        chats.clear();
    }

}

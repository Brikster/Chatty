package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import net.amoebaman.util.ArrayWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.JsonStorage;

public class ChatCommand extends BukkitCommand {

    private final ChatManager chatManager;
    private final JsonStorage jsonStorage;

    public ChatCommand(Configuration configuration, ChatManager chatManager, JsonStorage jsonStorage) {
        super("chat", ArrayWrapper.toArray(configuration.getNode("miscellaneous.commands.chat.aliases").getAsStringList(), String.class));

        this.chatManager = chatManager;
        this.jsonStorage = jsonStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("chatty.command.chat")) {
                sender.sendMessage(Chatty.instance().messages().get("no-permission"));
                return;
            }

            if (args.length == 1) {
                String chatName = args[0];
                Chat chat = chatManager.getChat(chatName);

                if (chat != null) {
                    if (!chat.isPermissionRequired()
                            || sender.hasPermission(String.format("chatty.chat.%s", chat.getName()))
                            || sender.hasPermission(String.format("chatty.chat.%s.write", chat.getName()))) {
                        jsonStorage.setProperty((Player) sender, "chat", new JsonPrimitive(chat.getName()));
                        sender.sendMessage(Chatty.instance().messages().get("chat-command.chat-switched").replace("{chat}", chat.getName()));
                    } else {
                        sender.sendMessage(Chatty.instance().messages().get("chat-command.no-chat-permission"));
                    }
                } else {
                    sender.sendMessage(Chatty.instance().messages().get("chat-command.chat-not-found"));
                }
            } else {
                sender.sendMessage(Chatty.instance().messages().get("chat-command.usage").replace("{label}", label));
            }
        } else {
            sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
        }
    }

}

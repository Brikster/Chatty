package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;

public class SpyCommand extends AbstractCommand {

    private final ChatManager chatManager;

    SpyCommand(ChatManager chatManager) {
        super("spy");

        this.chatManager = chatManager;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("chatty.command.spy")) {
                sender.sendMessage(Configuration.getMessages().get("no-permission"));
                return;
            }

            if (chatManager.getSpyDisabled().contains(sender)) {
                sender.sendMessage(Configuration.getMessages().get("spy-on"));
                chatManager.getSpyDisabled().remove(sender);
            } else {
                sender.sendMessage(Configuration.getMessages().get("spy-off"));
                chatManager.getSpyDisabled().add((Player) sender);
            }
        } else sender.sendMessage(Configuration.getMessages().get("only-for-players"));
    }

}

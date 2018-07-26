package ru.mrbrikster.chatty.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;

public class SpyCommand extends AbstractCommand {

    private final Configuration configuration;
    private final ChatManager chatManager;

    SpyCommand(Configuration configuration, ChatManager chatManager) {
        super("spy");

        this.configuration = configuration;
        this.chatManager = chatManager;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("chatty.command.spy")) {
                sender.sendMessage(configuration.getNode("messages.no-permission")
                        .getAsString(ChatColor.RED + "You don't have permission."));
                return;
            }

            if (chatManager.getSpyDisabled().contains(sender)) {
                sender.sendMessage(configuration.getNode("messages.spy-on")
                        .getAsString(ChatColor.GREEN + "You have been enabled spy-mode."));
                chatManager.getSpyDisabled().remove(sender);
            } else {
                sender.sendMessage(configuration.getNode("messages.spy-off")
                        .getAsString(ChatColor.RED + "You have been disabled spy-mode."));
                chatManager.getSpyDisabled().add((Player) sender);
            }
        } else sender.sendMessage("Only for players.");
    }

}

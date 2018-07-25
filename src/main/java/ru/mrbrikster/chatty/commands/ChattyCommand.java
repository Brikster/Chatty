package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;

public class ChattyCommand extends AbstractCommand {

    protected ChattyCommand() {
        super("chatty");
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        /*
        if (sender.hasPermission("chatty.command.chatty")) {
            Chatty.instance().init();
            sender.sendMessage(Chatty.instance().getConfiguration().getMessages().getOrDefault("reload",
                    ChatColor.GREEN + "Config successful reloaded!"));
        } else {
            sender.sendMessage(Chatty.instance().getConfiguration().getMessages().getOrDefault("no-permission",
                    ChatColor.RED + "You don't have permission."));
        }
        */
    }

}

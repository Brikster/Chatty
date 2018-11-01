package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

public class ChattyCommand extends BukkitCommand {

    private final Configuration configuration;

    ChattyCommand(Configuration configuration) {
        super("chatty");

        this.configuration = configuration;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("chatty.command.reload")) {
            configuration.reload();
            sender.sendMessage(Chatty.instance().getMessages().get("reload"));
        } else sender.sendMessage(Chatty.instance().getMessages().get("no-permission"));
    }

}

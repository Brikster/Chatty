package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;
import ru.mrbrikster.chatty.config.Configuration;

public class ChattyCommand extends AbstractCommand {

    private final Configuration configuration;

    ChattyCommand(Configuration configuration) {
        super("chatty");

        this.configuration = configuration;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("chatty.command.chatty")) {
            configuration.reload();
            sender.sendMessage(Configuration.getMessages().get("reload"));
        } else sender.sendMessage(Configuration.getMessages().get("no-permission"));
    }

}

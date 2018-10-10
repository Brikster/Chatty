package ru.mrbrikster.chatty.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.mrbrikster.chatty.config.Configuration;

public class ClearChatCommand extends AbstractCommand {

    ClearChatCommand() {
        super("clearchat", "cc");
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("chatty.command.clearchat")) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage(" ");
                }

                player.sendMessage(Configuration.getMessages().get("chat-cleared").replace("{player}", sender.getName()));
            });
        } else sender.sendMessage(Configuration.getMessages().get("no-permission"));
    }

}

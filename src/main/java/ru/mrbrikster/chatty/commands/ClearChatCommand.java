package ru.mrbrikster.chatty.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;

public class ClearChatCommand extends BukkitCommand {

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

                player.sendMessage(Chatty.instance().messages().get("chat-cleared").replace("{player}", sender.getName()));
            });
        } else sender.sendMessage(Chatty.instance().messages().get("no-permission"));
    }

}

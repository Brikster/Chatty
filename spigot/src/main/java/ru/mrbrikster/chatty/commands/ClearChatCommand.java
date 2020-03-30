package ru.mrbrikster.chatty.commands;

import net.amoebaman.util.ArrayWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;

public class ClearChatCommand extends BukkitCommand {

    ClearChatCommand(Configuration configuration) {
        super("clearchat", ArrayWrapper.toArray(configuration.getNode("miscellaneous.commands.clearchat.aliases").getAsStringList(), String.class));
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("chatty.command.clearchat")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    for (int i = 0; i < 100; i++) {
                        sender.sendMessage(" ");
                    }

                    sender.sendMessage(Chatty.instance().messages().get("clearchat-command.clear-chat-for-yourself"));
                } else {
                    sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
                }
            } else {
                if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
                    if (sender.hasPermission("chatty.command.clearchat.all")) {
                        String chatClearedMessage = Chatty.instance().messages().get("clearchat-command.clear-chat-for-all").replace("{player}", sender.getName());

                        Reflection.getOnlinePlayers().forEach(player -> {
                            for (int i = 0; i < 100; i++) {
                                player.sendMessage(" ");
                            }

                            player.sendMessage(chatClearedMessage);
                        });
                    } else {
                        sender.sendMessage(Chatty.instance().messages().get("no-permission"));
                    }
                } else {
                    sender.sendMessage(Chatty.instance().messages().get("clearchat-command.usage").replace("{label}", label));
                }
            }
        } else {
            sender.sendMessage(Chatty.instance().messages().get("no-permission"));
        }
    }

}

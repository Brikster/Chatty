package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.TemporaryStorage;

public class SpyCommand extends BukkitCommand {

    private final TemporaryStorage temporaryStorage;

    SpyCommand(TemporaryStorage temporaryStorage) {
        super("spy");

        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("chatty.command.spy")) {
                sender.sendMessage(Chatty.instance().getMessages().get("no-permission"));
                return;
            }

            if (temporaryStorage.getSpyDisabled().contains(sender)) {
                sender.sendMessage(Chatty.instance().getMessages().get("spy-on"));
                temporaryStorage.getSpyDisabled().remove(sender);
            } else {
                sender.sendMessage(Chatty.instance().getMessages().get("spy-off"));
                temporaryStorage.getSpyDisabled().add((Player) sender);
            }
        } else sender.sendMessage(Chatty.instance().getMessages().get("only-for-players"));
    }

}

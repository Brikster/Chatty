package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.config.Configuration;

public class SpyCommand extends AbstractCommand {

    private final TemporaryStorage temporaryStorage;

    SpyCommand(TemporaryStorage temporaryStorage) {
        super("spy");

        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("chatty.command.spy")) {
                sender.sendMessage(Configuration.getMessages().get("no-permission"));
                return;
            }

            if (temporaryStorage.getSpyDisabled().contains(sender)) {
                sender.sendMessage(Configuration.getMessages().get("spy-on"));
                temporaryStorage.getSpyDisabled().remove(sender);
            } else {
                sender.sendMessage(Configuration.getMessages().get("spy-off"));
                temporaryStorage.getSpyDisabled().add((Player) sender);
            }
        } else sender.sendMessage(Configuration.getMessages().get("only-for-players"));
    }

}

package ru.mrbrikster.chatty.commands.pm;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.commands.AbstractCommand;
import ru.mrbrikster.chatty.config.Configuration;

import java.util.Arrays;

public class MsgCommand extends AbstractCommand {

    private final MessagesStorage messagesStorage;

    public MsgCommand(MessagesStorage messagesStorage) {
        super("msg", "message", "pm", "m");

        this.messagesStorage = messagesStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Configuration.getMessages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.msg")) {
            sender.sendMessage(Configuration.getMessages().get("no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Configuration.getMessages().get("msg-command.usage")
                .replace("{label}", label));
            return;
        }

        String recipient = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Player playerRecipient = Bukkit.getPlayer(recipient);

        if (playerRecipient == null) {
            sender.sendMessage(Configuration.getMessages().get("msg-command.player-not-found"));
            return;
        }

        if (playerRecipient.equals(sender)) {
            sender.sendMessage(Configuration.getMessages().get("msg-command.cannot-message-yourself"));
            return;
        }

        playerRecipient.sendMessage(
                Configuration.getMessages().get("msg-command.recipient-format")
                    .replace("{sender}", sender.getName())
                    .replace("{recipient}", playerRecipient.getName())
                    .replace("{message}", message)
        );

        sender.sendMessage(
                Configuration.getMessages().get("msg-command.sender-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", playerRecipient.getName())
                        .replace("{message}", message)
        );

        messagesStorage.setLastMessaged(playerRecipient, (Player) sender);
        messagesStorage.setLastMessaged((Player) sender, playerRecipient);
    }

}

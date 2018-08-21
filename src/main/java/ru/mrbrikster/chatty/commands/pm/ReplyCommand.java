package ru.mrbrikster.chatty.commands.pm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.commands.AbstractCommand;
import ru.mrbrikster.chatty.config.Configuration;

import java.util.Optional;

public class ReplyCommand extends AbstractCommand {

    private final MessagesStorage messagesStorage;

    public ReplyCommand(MessagesStorage messagesStorage) {
        super("reply", "r");

        this.messagesStorage = messagesStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Configuration.getMessages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.reply")) {
            sender.sendMessage(Configuration.getMessages().get("no-permission"));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Configuration.getMessages().get("reply-command.usage")
                    .replace("{label}", label));
            return;
        }

        String message = String.join(" ", args);

        Optional<Player> optionalRecipient = messagesStorage.getLastMessaged((Player) sender);
        if (!optionalRecipient.isPresent()) {
            sender.sendMessage(Configuration.getMessages().get("reply-command.target-not-found"));
            return;
        }

        Player playerRecipient = optionalRecipient.get();

        playerRecipient.sendMessage(
                Configuration.getMessages().get("reply-command.recipient-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", playerRecipient.getName())
                        .replace("{message}", message)
        );

        sender.sendMessage(
                Configuration.getMessages().get("reply-command.sender-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", playerRecipient.getName())
                        .replace("{message}", message)
        );

        messagesStorage.setLastMessaged(playerRecipient, (Player) sender);
    }

}

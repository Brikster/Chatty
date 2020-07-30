package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonElement;
import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReplyCommand extends PrivateMessageCommand {

    public ReplyCommand(Chatty chatty) {
        super(chatty, "r",
                ArrayWrapper.toArray(chatty.getExact(Configuration.class).getNode("pm.commands.reply.aliases").getAsStringList()
                        .stream().map(alias -> {
                            if (alias.equalsIgnoreCase("r")) {
                                chatty.getLogger().log(Level.WARNING, "Please, rename \"r\" alias to \"reply\" in reply command configuration. " +
                                        "This change was made due to EssentialsX with default command name \"r\" instead of \"reply\"");

                                return "reply";
                            }

                            return alias;
                        }).collect(Collectors.toList()), String.class));
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.reply")) {
            sender.sendMessage(Chatty.instance().messages().get("no-permission"));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.usage")
                    .replace("{label}", label));
            return;
        }

        String message = String.join(" ", args);

        Optional<String> optionalRecipient = jsonStorage.getProperty((Player) sender, "last-pm-interlocutor").map(JsonElement::getAsString);
        if (!optionalRecipient.isPresent()) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        CommandSender recipient = optionalRecipient.get().equalsIgnoreCase("CONSOLE")
                && configuration.getNode("pm.allow-console").getAsBoolean(false)
                ? Bukkit.getConsoleSender() : Bukkit.getPlayer(optionalRecipient.get());

        if (recipient == null) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        if (recipient instanceof Player
                && !configuration.getNode("pm.allow-pm-vanished").getAsBoolean(true)
                && ((Player) sender).canSee((Player) recipient)) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        handlePrivateMessage(sender, recipient, message);
    }

}

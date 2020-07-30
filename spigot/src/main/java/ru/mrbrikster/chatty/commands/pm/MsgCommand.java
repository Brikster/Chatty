package ru.mrbrikster.chatty.commands.pm;

import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

import java.util.Arrays;

public class MsgCommand extends PrivateMessageCommand {

    public MsgCommand(Chatty chatty) {
        super(chatty, "msg", ArrayWrapper.toArray(chatty.getExact(Configuration.class)
                .getNode("pm.commands.msg.aliases").getAsStringList(), String.class));
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        boolean isPlayer = sender instanceof Player;
        if (!isPlayer && !configuration.getNode("pm.allow-console").getAsBoolean(false)) {
            sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.msg")) {
            sender.sendMessage(Chatty.instance().messages().get("no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Chatty.instance().messages().get("msg-command.usage")
                    .replace("{label}", label));
            return;
        }

        String recipientName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        CommandSender recipient =
                recipientName.equalsIgnoreCase("CONSOLE") && configuration.getNode("pm.allow-console").getAsBoolean(false)
                        ? Bukkit.getConsoleSender() : Bukkit.getPlayer(recipientName);

        if (recipient == null) {
            sender.sendMessage(Chatty.instance().messages().get("msg-command.player-not-found"));
            return;
        }

        if (recipient.equals(sender)) {
            sender.sendMessage(Chatty.instance().messages().get("msg-command.cannot-message-yourself"));
            return;
        }

        if (recipient instanceof Player && isPlayer
                && !configuration.getNode("pm.allow-pm-vanished").getAsBoolean(true)
                && ((Player) sender).canSee((Player) recipient)) {
            sender.sendMessage(Chatty.instance().messages().get("msg-command.player-not-found"));
            return;
        }

        handlePrivateMessage(sender, recipient, message);
    }

}

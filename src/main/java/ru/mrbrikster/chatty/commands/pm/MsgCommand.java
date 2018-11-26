package ru.mrbrikster.chatty.commands.pm;

import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;

import java.util.Arrays;

public class MsgCommand extends BukkitCommand {

    private final Configuration configuration;
    private final TemporaryStorage commandsStorage;
    private final PermanentStorage permanentStorage;

    public MsgCommand(
            Configuration configuration,
            TemporaryStorage commandsStorage,
            PermanentStorage permanentStorage) {
        super("msg", ArrayWrapper.toArray(configuration.getNode("commands.msg.aliases").getAsStringList(), String.class));

        this.configuration = configuration;
        this.commandsStorage = commandsStorage;
        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player) && !configuration.getNode("commands.msg.allow-console").getAsBoolean(false)) {
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
                recipientName.equalsIgnoreCase("CONSOLE") && configuration.getNode("commands.msg.allow-console").getAsBoolean(false)
                        ? Bukkit.getConsoleSender() : Bukkit.getPlayer(recipientName);

        if (recipient == null) {
            sender.sendMessage(Chatty.instance().messages().get("msg-command.player-not-found"));
            return;
        }

        if (recipient.equals(sender)) {
            sender.sendMessage(Chatty.instance().messages().get("msg-command.cannot-message-yourself"));
            return;
        }

        if (!permanentStorage.isIgnore(recipient, sender))
            recipient.sendMessage(
                    Chatty.instance().messages().get("msg-command.recipient-format")
                            .replace("{sender}", sender.getName())
                            .replace("{recipient}", recipient.getName())
                            .replace("{message}", message)
            );

        sender.sendMessage(
                Chatty.instance().messages().get("msg-command.sender-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", recipient.getName())
                        .replace("{message}", message)
        );

        commandsStorage.setLastMessaged(recipient.getName(), sender.getName());
        commandsStorage.setLastMessaged(sender.getName(), recipient.getName());

        Bukkit.getOnlinePlayers().stream()
                .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(recipient))
                .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
                .filter(spyPlayer -> !commandsStorage.getSpyDisabled().contains(spyPlayer))
                .forEach(spyPlayer -> spyPlayer.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', configuration.getNode("general.spy.pm-format")
                                .getAsString("&6[Spy] &7{sender} &6-> &7{recipient}: &f{message}"))
                                .replace("{sender}", sender.getName())
                                .replace("{recipient}", recipient.getName())
                                .replace("{message}", message)
                ));
    }

}

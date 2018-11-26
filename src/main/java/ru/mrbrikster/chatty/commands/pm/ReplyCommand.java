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

import java.util.Optional;

public class ReplyCommand extends BukkitCommand {

    private final Configuration configuration;
    private final TemporaryStorage commandsStorage;
    private final PermanentStorage permanentStorage;

    public ReplyCommand(
            Configuration configuration,
            TemporaryStorage commandsStorage,
            PermanentStorage permanentStorage) {
        super("reply", ArrayWrapper.toArray(configuration.getNode("commands.reply.aliases").getAsStringList(), String.class));

        this.configuration = configuration;
        this.commandsStorage = commandsStorage;
        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player) && !configuration.getNode("commands.reply.allow-console").getAsBoolean(false)) {
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

        Optional<String> optionalRecipient = commandsStorage.getLastMessaged(sender.getName());
        if (!optionalRecipient.isPresent()) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        CommandSender recipient = optionalRecipient.get().equalsIgnoreCase("CONSOLE")
                && configuration.getNode("commands.reply.allow-console").getAsBoolean(false)
                ? Bukkit.getConsoleSender() : Bukkit.getPlayer(optionalRecipient.get());

        if (recipient == null) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        if (!permanentStorage.isIgnore(recipient, sender))
            recipient.sendMessage(
                    Chatty.instance().messages().get("reply-command.recipient-format")
                            .replace("{sender}", sender.getName())
                            .replace("{recipient}", recipient.getName())
                            .replace("{message}", message)
            );

        sender.sendMessage(
                Chatty.instance().messages().get("reply-command.sender-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", recipient.getName())
                        .replace("{message}", message)
        );

        commandsStorage.setLastMessaged(recipient.getName(), sender.getName());

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

package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.commands.AbstractCommand;
import ru.mrbrikster.chatty.config.Configuration;

import java.util.Arrays;

public class MsgCommand extends AbstractCommand {

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

        String recipientName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        CommandSender recipient =
                recipientName.equalsIgnoreCase("CONSOLE") && configuration.getNode("commands.msg.allow-console").getAsBoolean(false)
                        ? Bukkit.getConsoleSender() : Bukkit.getPlayer(recipientName);

        if (recipient == null) {
            sender.sendMessage(Configuration.getMessages().get("msg-command.player-not-found"));
            return;
        }

        if (recipient.equals(sender)) {
            sender.sendMessage(Configuration.getMessages().get("msg-command.cannot-message-yourself"));
            return;
        }

        sender.sendMessage(
                Configuration.getMessages().get("msg-command.sender-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", recipient.getName())
                        .replace("{message}", message)
        );

        if (recipient instanceof Player) {
            JsonElement jsonElement = permanentStorage.getProperty((Player) recipient, "ignore").orElseGet(JsonArray::new);

            if (!jsonElement.isJsonArray())
                jsonElement = new JsonArray();

            if (!jsonElement.getAsJsonArray().contains(new JsonPrimitive(sender.getName())))
                recipient.sendMessage(
                        Configuration.getMessages().get("msg-command.recipient-format")
                                .replace("{sender}", sender.getName())
                                .replace("{recipient}", recipient.getName())
                                .replace("{message}", message)
                );
        }


        if (sender instanceof Player && recipient instanceof Player) {
            commandsStorage.setLastMessaged((Player) recipient, (Player) sender);
            commandsStorage.setLastMessaged((Player) sender, (Player) recipient);
        }

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

package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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
        super("msg", "message", "pm", "m", "w");

        this.configuration = configuration;
        this.commandsStorage = commandsStorage;
        this.permanentStorage = permanentStorage;
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

        sender.sendMessage(
                Configuration.getMessages().get("msg-command.sender-format")
                        .replace("{sender}", sender.getName())
                        .replace("{recipient}", playerRecipient.getName())
                        .replace("{message}", message)
        );

        JsonElement jsonElement = permanentStorage.getProperty(playerRecipient, "ignore").orElseGet(JsonArray::new);

        if (!jsonElement.isJsonArray())
            jsonElement = new JsonArray();

        if (!jsonElement.getAsJsonArray().contains(new JsonPrimitive(sender.getName())))
            playerRecipient.sendMessage(
                    Configuration.getMessages().get("msg-command.recipient-format")
                            .replace("{sender}", sender.getName())
                            .replace("{recipient}", playerRecipient.getName())
                            .replace("{message}", message)
            );

        commandsStorage.setLastMessaged(playerRecipient, (Player) sender);
        commandsStorage.setLastMessaged((Player) sender, playerRecipient);

        Bukkit.getOnlinePlayers().stream()
                .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(playerRecipient))
                .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
                .filter(spyPlayer -> !commandsStorage.getSpyDisabled().contains(spyPlayer))
                .forEach(spyPlayer -> spyPlayer.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', configuration.getNode("general.spy.pm-format")
                                .getAsString("&6[Spy] &7{sender} &6-> &7{recipient}: &f{message}"))
                                .replace("{sender}", sender.getName())
                                .replace("{recipient}", playerRecipient.getName())
                                .replace("{message}", message)
                ));
    }

}

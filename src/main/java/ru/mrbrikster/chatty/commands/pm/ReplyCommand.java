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

import java.util.Optional;

public class ReplyCommand extends AbstractCommand {

    private final Configuration configuration;
    private final TemporaryStorage commandsStorage;
    private final PermanentStorage permanentStorage;

    public ReplyCommand(
            Configuration configuration,
            TemporaryStorage commandsStorage,
            PermanentStorage permanentStorage) {
        super("msg", ArrayWrapper.toArray(configuration.getNode("commands.reply.aliases").getAsStringList(), String.class));

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

        Optional<Player> optionalRecipient = commandsStorage.getLastMessaged((Player) sender);
        if (!optionalRecipient.isPresent()) {
            sender.sendMessage(Configuration.getMessages().get("reply-command.target-not-found"));
            return;
        }

        Player playerRecipient = optionalRecipient.get();

        JsonElement jsonElement = permanentStorage.getProperty(playerRecipient, "ignore").orElseGet(JsonArray::new);

        if (!jsonElement.isJsonArray())
            jsonElement = new JsonArray();

        if (!jsonElement.getAsJsonArray().contains(new JsonPrimitive(sender.getName())))
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

        commandsStorage.setLastMessaged(playerRecipient, (Player) sender);

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

package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.commands.AbstractCommand;
import ru.mrbrikster.chatty.config.Configuration;

public class IgnoreCommand extends AbstractCommand {

    private final PermanentStorage permanentStorage;

    public IgnoreCommand(PermanentStorage permanentStorage) {
        super("ignore");

        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Configuration.getMessages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.ignore")) {
            sender.sendMessage(Configuration.getMessages().get("no-permission"));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(Configuration.getMessages().get("ignore-command.usage")
                    .replace("{label}", label));
            return;
        }

        String ignoreTarget = args[0];

        Player ignoreTargetPlayer = Bukkit.getPlayer(ignoreTarget);

        if (ignoreTargetPlayer == null) {
            sender.sendMessage(Configuration.getMessages().get("ignore-command.player-not-found")
                    .replace("{label}", label));
            return;
        }

        if (sender.equals(ignoreTargetPlayer)) {
            sender.sendMessage(Configuration.getMessages().get("ignore-command.cannot-ignore-yourself")
                    .replace("{label}", label));
            return;
        }

        JsonElement jsonElement = permanentStorage.getProperty((Player) sender, "ignore").orElseGet(JsonArray::new);

        if (!jsonElement.isJsonArray())
            jsonElement = new JsonArray();

        if (jsonElement.getAsJsonArray().contains(new JsonPrimitive(ignoreTargetPlayer.getName()))) {
            sender.sendMessage(Configuration.getMessages().get("ignore-command.remove-ignore")
                    .replace("{label}", label).replace("{player}", ignoreTargetPlayer.getName()));
            ((JsonArray) jsonElement).remove(new JsonPrimitive(ignoreTargetPlayer.getName()));
        } else {
            sender.sendMessage(Configuration.getMessages().get("ignore-command.add-ignore")
                    .replace("{label}", label).replace("{player}", ignoreTargetPlayer.getName()));
            jsonElement.getAsJsonArray().add(ignoreTargetPlayer.getName());
        }

        permanentStorage.setProperty((Player) sender, "ignore", jsonElement);
    }

}

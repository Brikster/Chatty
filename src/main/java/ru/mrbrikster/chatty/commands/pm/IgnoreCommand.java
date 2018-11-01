package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.PermanentStorage;

public class IgnoreCommand extends BukkitCommand {

    private final PermanentStorage permanentStorage;

    public IgnoreCommand(
            Configuration configuration,
            PermanentStorage permanentStorage) {
        super("ignore", ArrayWrapper.toArray(configuration.getNode("commands.ignore.aliases").getAsStringList(), String.class));

        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Chatty.instance().getMessages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.ignore")) {
            sender.sendMessage(Chatty.instance().getMessages().get("no-permission"));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(Chatty.instance().getMessages().get("ignore-command.usage")
                    .replace("{label}", label));
            return;
        }

        String ignoreTarget = args[0];

        Player ignoreTargetPlayer = Bukkit.getPlayer(ignoreTarget);

        if (ignoreTargetPlayer == null) {
            sender.sendMessage(Chatty.instance().getMessages().get("ignore-command.player-not-found")
                    .replace("{label}", label));
            return;
        }

        if (sender.equals(ignoreTargetPlayer)) {
            sender.sendMessage(Chatty.instance().getMessages().get("ignore-command.cannot-ignore-yourself")
                    .replace("{label}", label));
            return;
        }

        JsonElement jsonElement = permanentStorage.getProperty((Player) sender, "ignore").orElseGet(JsonArray::new);

        if (!jsonElement.isJsonArray())
            jsonElement = new JsonArray();

        if (jsonElement.getAsJsonArray().contains(new JsonPrimitive(ignoreTargetPlayer.getName()))) {
            sender.sendMessage(Chatty.instance().getMessages().get("ignore-command.remove-ignore")
                    .replace("{label}", label).replace("{player}", ignoreTargetPlayer.getName()));
            ((JsonArray) jsonElement).remove(new JsonPrimitive(ignoreTargetPlayer.getName()));
        } else {
            sender.sendMessage(Chatty.instance().getMessages().get("ignore-command.add-ignore")
                    .replace("{label}", label).replace("{player}", ignoreTargetPlayer.getName()));
            jsonElement.getAsJsonArray().add(ignoreTargetPlayer.getName());
        }

        permanentStorage.setProperty((Player) sender, "ignore", jsonElement);
    }

}

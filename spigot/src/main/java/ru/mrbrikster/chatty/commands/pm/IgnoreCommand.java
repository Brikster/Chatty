package ru.mrbrikster.chatty.commands.pm;

import com.google.common.base.Joiner;
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
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.util.HashSet;
import java.util.Set;

public class IgnoreCommand extends BukkitCommand {

    private final JsonStorage jsonStorage;

    public IgnoreCommand(
            Configuration configuration,
            JsonStorage jsonStorage) {
        super("ignore", ArrayWrapper.toArray(configuration.getNode("pm.commands.ignore.aliases").getAsStringList(), String.class));

        this.jsonStorage = jsonStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
            return;
        }

        if (!sender.hasPermission("chatty.command.ignore")) {
            sender.sendMessage(Chatty.instance().messages().get("no-permission"));
            return;
        }

        if (args.length != 1) {
            if (args.length == 0) {
                JsonElement jsonElement = jsonStorage.getProperty((Player) sender, "ignore").orElseGet(JsonArray::new);

                if (!jsonElement.isJsonArray())
                    jsonElement = new JsonArray();

                Set<String> ignoreList = new HashSet<>();
                for (JsonElement element : jsonElement.getAsJsonArray()) {
                    ignoreList.add(element.getAsString());
                }

                if (!ignoreList.isEmpty()) {
                    String joinedIgnoreList = Joiner.on(Chatty.instance().messages().get("ignore-command.ignore-list-delimiter"))
                            .join(ignoreList);

                    sender.sendMessage(Chatty.instance().messages().get("ignore-command.ignore-list").replace("{players}", joinedIgnoreList));
                }
            }

            sender.sendMessage(Chatty.instance().messages().get("ignore-command.usage")
                    .replace("{label}", label));
            return;
        }

        String ignoreTarget = args[0];

        if (sender.getName().equalsIgnoreCase(ignoreTarget)) {
            sender.sendMessage(Chatty.instance().messages().get("ignore-command.cannot-ignore-yourself")
                    .replace("{label}", label));
            return;
        }

        JsonElement jsonElement = jsonStorage.getProperty((Player) sender, "ignore").orElseGet(JsonArray::new);

        if (!jsonElement.isJsonArray())
            jsonElement = new JsonArray();

        Player ignoreTargetPlayer = Bukkit.getPlayer(ignoreTarget);

        if (jsonElement.getAsJsonArray().contains(new JsonPrimitive(ignoreTarget.toLowerCase()))) {
            sender.sendMessage(Chatty.instance().messages().get("ignore-command.remove-ignore")
                    .replace("{label}", label).replace("{player}", ignoreTargetPlayer == null ? ignoreTarget : ignoreTargetPlayer.getName()));
            ((JsonArray) jsonElement).remove(new JsonPrimitive(ignoreTarget.toLowerCase()));
        } else {
            if (ignoreTargetPlayer == null) {
                sender.sendMessage(Chatty.instance().messages().get("ignore-command.player-not-found")
                        .replace("{label}", label));
                return;
            }

            sender.sendMessage(Chatty.instance().messages().get("ignore-command.add-ignore")
                    .replace("{label}", label).replace("{player}", ignoreTargetPlayer.getName()));
            jsonElement.getAsJsonArray().add(ignoreTargetPlayer.getName().toLowerCase());
        }

        jsonStorage.setProperty((Player) sender, "ignore", jsonElement);
    }

}

package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;

public class SpyCommand extends BukkitCommand {

    private final JsonStorage jsonStorage;

    SpyCommand(JsonStorage jsonStorage) {
        super("spy");

        this.jsonStorage = jsonStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("chatty.command.spy")) {
                sender.sendMessage(Chatty.instance().messages().get("no-permission"));
                return;
            }

            if (jsonStorage.getProperty((Player) sender, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean()) {
                jsonStorage.setProperty((Player) sender, "spy-mode", new JsonPrimitive(false));
                sender.sendMessage(Chatty.instance().messages().get("spy-off"));
            } else {
                jsonStorage.setProperty((Player) sender, "spy-mode", new JsonPrimitive(true));
                sender.sendMessage(Chatty.instance().messages().get("spy-on"));
            }
        } else {
            sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
        }
    }

}

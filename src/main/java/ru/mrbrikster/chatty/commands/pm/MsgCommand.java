package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonPrimitive;
import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PrefixAndSuffixManager;

import java.util.Arrays;

public class MsgCommand extends BukkitCommand {

    private final Configuration configuration;
    private final JsonStorage jsonStorage;

    private final PrefixAndSuffixManager prefixAndSuffixManager;

    public MsgCommand(
            Configuration configuration,
            DependencyManager dependencyManager,
            JsonStorage jsonStorage) {
        super("msg", ArrayWrapper.toArray(configuration.getNode("pm.commands.msg.aliases").getAsStringList(), String.class));

        this.configuration = configuration;
        this.jsonStorage = jsonStorage;

        this.prefixAndSuffixManager = new PrefixAndSuffixManager(dependencyManager, jsonStorage);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player) && !configuration.getNode("pm.allow-console").getAsBoolean(false)) {
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

        String recipientPrefix, recipientSuffix;
        if (recipient instanceof Player) {
            recipientName = ((Player) recipient).getDisplayName();
            recipientPrefix = prefixAndSuffixManager.getPrefix((Player) recipient);
            recipientSuffix = prefixAndSuffixManager.getSuffix((Player) recipient);
            jsonStorage.setProperty((Player) recipient, "last-pm-interlocutor", new JsonPrimitive(sender.getName()));
        } else {
            recipientName = recipient.getName();
            recipientPrefix = "";
            recipientSuffix = "";
        }

        String senderName, senderPrefix, senderSuffix;
        if (sender instanceof Player) {
            senderName = ((Player) sender).getDisplayName();
            senderPrefix = prefixAndSuffixManager.getPrefix((Player) sender);
            senderSuffix = prefixAndSuffixManager.getSuffix((Player) sender);
            jsonStorage.setProperty((Player) sender, "last-pm-interlocutor", new JsonPrimitive(recipientName));
        } else {
            senderName = sender.getName();
            senderPrefix = "";
            senderSuffix = "";
        }

        String senderFormat;
        if (!jsonStorage.isIgnore(recipient, sender)) {
            recipient.sendMessage(Chatty.instance().messages().get("pm.format.recipient")
                    .replace("{sender-prefix}", senderPrefix)
                    .replace("{sender-suffix}", senderSuffix)
                    .replace("{sender-name}", senderName)
                    .replace("{recipient-name}", recipientName)
                    .replace("{recipient-prefix}", recipientPrefix)
                    .replace("{recipient-suffix}", recipientSuffix)
                    .replace("{message}", message));
        }

        sender.sendMessage(senderFormat = Chatty.instance().messages().get("pm.format.sender")
                .replace("{sender-prefix}", senderPrefix)
                .replace("{sender-suffix}", senderSuffix)
                .replace("{sender-name}", senderName)
                .replace("{recipient-name}", recipientName)
                .replace("{recipient-prefix}", recipientPrefix)
                .replace("{recipient-suffix}", recipientSuffix)
                .replace("{message}", message));

        Bukkit.getOnlinePlayers().stream()
                .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(recipient))
                .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
                .filter(spyPlayer -> jsonStorage.getProperty(spyPlayer, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean())
                .forEach(spyPlayer -> spyPlayer.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', configuration.getNode("spy.format.pm")
                                .getAsString("&6[Spy] &r{format}"))
                                .replace("{format}", senderFormat)
                ));
    }

}

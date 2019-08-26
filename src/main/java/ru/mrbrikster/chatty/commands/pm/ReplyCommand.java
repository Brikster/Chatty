package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonElement;
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

import java.util.Optional;

public class ReplyCommand extends BukkitCommand {

    private final Configuration configuration;
    private final JsonStorage jsonStorage;
    private final PrefixAndSuffixManager prefixAndSuffixManager;

    public ReplyCommand(
            Configuration configuration,
            DependencyManager dependencyManager,
            JsonStorage jsonStorage) {
        super("reply", ArrayWrapper.toArray(configuration.getNode("pm.commands.reply.aliases").getAsStringList(), String.class));

        this.configuration = configuration;
        this.jsonStorage = jsonStorage;

        this.prefixAndSuffixManager = new PrefixAndSuffixManager(dependencyManager, jsonStorage);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
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

        Optional<String> optionalRecipient = jsonStorage.getProperty((Player) sender, "last-pm-interlocutor").map(JsonElement::getAsString);
        if (!optionalRecipient.isPresent()) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        CommandSender recipient = optionalRecipient.get().equalsIgnoreCase("CONSOLE")
                && configuration.getNode("pm.allow-console").getAsBoolean(false)
                ? Bukkit.getConsoleSender() : Bukkit.getPlayer(optionalRecipient.get());

        if (recipient == null) {
            sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
            return;
        }

        String recipientName, recipientPrefix, recipientSuffix;
        if (recipient instanceof Player) {
            Player recipientPlayer = ((Player) recipient);

            recipientName = recipientPlayer.getDisplayName();
            recipientPrefix = prefixAndSuffixManager.getPrefix(recipientPlayer);
            recipientSuffix = prefixAndSuffixManager.getSuffix(recipientPlayer);
            jsonStorage.setProperty(recipientPlayer, "last-pm-interlocutor", new JsonPrimitive(sender.getName()));
        } else {
            recipientName = recipient.getName();
            recipientPrefix = "";
            recipientSuffix = "";
        }

        String senderName = ((Player) sender).getDisplayName();
        String senderPrefix = prefixAndSuffixManager.getPrefix((Player) sender);
        String senderSuffix = prefixAndSuffixManager.getSuffix((Player) sender);
        jsonStorage.setProperty((Player) sender, "last-pm-interlocutor", new JsonPrimitive(recipientName));

        if (!jsonStorage.isIgnore(recipient, sender)) {
            recipient.sendMessage(
                    Chatty.instance().messages().get("pm.format.recipient")
                            .replace("{sender-prefix}", senderPrefix)
                            .replace("{sender-suffix}", senderSuffix)
                            .replace("{sender-name}", senderName)
                            .replace("{recipient-name}", recipientName)
                            .replace("{recipient-prefix}", recipientPrefix)
                            .replace("{recipient-suffix}", recipientSuffix)
                            .replace("{message}", message)
            );
        }

        sender.sendMessage(
                Chatty.instance().messages().get("pm.format.sender")
                        .replace("{sender-prefix}", senderPrefix)
                        .replace("{sender-suffix}", senderSuffix)
                        .replace("{sender-name}", senderName)
                        .replace("{recipient-name}", recipientName)
                        .replace("{recipient-prefix}", recipientPrefix)
                        .replace("{recipient-suffix}", recipientSuffix)
                        .replace("{message}", message)
        );

        Bukkit.getOnlinePlayers().stream()
                .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(recipient))
                .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
                .filter(spyPlayer -> jsonStorage.getProperty(spyPlayer, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean())
                .forEach(spyPlayer -> spyPlayer.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', configuration.getNode("spy.format.pm")
                                .getAsString("&6[Spy] &7{sender} &6-> &7{recipient}: &f{message}"))
                                .replace("{sender}", senderName)
                                .replace("{recipient}", recipientName)
                                .replace("{message}", message)
                ));
    }

}

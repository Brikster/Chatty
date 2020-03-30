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
import ru.mrbrikster.chatty.moderation.AdvertisementModerationMethod;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;

import java.util.Optional;

public class ReplyCommand extends BukkitCommand {

    private final Configuration configuration;
    private final JsonStorage jsonStorage;
    private final PrefixAndSuffixManager prefixAndSuffixManager;
    private final ModerationManager moderationManager;

    public ReplyCommand(
            Configuration configuration,
            DependencyManager dependencyManager,
            JsonStorage jsonStorage,
            ModerationManager moderationManager) {
        super("reply", ArrayWrapper.toArray(configuration.getNode("pm.commands.reply.aliases").getAsStringList(), String.class));

        this.configuration = configuration;
        this.jsonStorage = jsonStorage;

        this.prefixAndSuffixManager = new PrefixAndSuffixManager(dependencyManager, jsonStorage);
        this.moderationManager = moderationManager;
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

        boolean cancelledByModeration = false;
        if (moderationManager.isSwearModerationEnabled()) {
            SwearModerationMethod swearMethod = moderationManager.getSwearMethod(message);
            if (!sender.hasPermission("chatty.moderation.swear")) {
                if (swearMethod.isBlocked()) {
                    message = swearMethod.getEditedMessage();

                    if (swearMethod.isUseBlock()) {
                        cancelledByModeration = true;
                    } else {
                        message = swearMethod.getEditedMessage();
                    }

                    String swearFound = Chatty.instance().messages().get("swear-found", null);

                    if (swearFound != null)
                        Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                                () -> sender.sendMessage(swearFound), 5L);
                }
            }
        }

        if (this.moderationManager.isAdvertisementModerationEnabled()) {
            AdvertisementModerationMethod advertisementMethod = this.moderationManager.getAdvertisementMethod(message);
            if (!sender.hasPermission("chatty.moderation.advertisement")) {
                if (advertisementMethod.isBlocked()) {
                    message = advertisementMethod.getEditedMessage();

                    if (advertisementMethod.isUseBlock()) {
                        cancelledByModeration = true;
                    } else {
                        message = advertisementMethod.getEditedMessage();
                    }

                    String adsFound = Chatty.instance().messages().get("advertisement-found", null);

                    if (adsFound != null)
                        Bukkit.getScheduler().runTaskLater(Chatty.instance(),
                                () -> sender.sendMessage(adsFound), 5L);
                }
            }
        }

        if (cancelledByModeration) {
            return;
        }

        if (!jsonStorage.isIgnore(recipient, sender)) {
            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', configuration.getNode("pm.format.recipient")
                    .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}")
                            .replace("{sender-prefix}", senderPrefix)
                            .replace("{sender-suffix}", senderSuffix)
                            .replace("{sender-name}", senderName)
                            .replace("{recipient-name}", recipientName)
                            .replace("{recipient-prefix}", recipientPrefix)
                            .replace("{recipient-suffix}", recipientSuffix))
                            .replace("{message}", message)
            );
        }

        String senderFormat;
        sender.sendMessage(senderFormat = ChatColor.translateAlternateColorCodes('&', configuration.getNode("pm.format.sender")
                .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}")
                        .replace("{sender-prefix}", senderPrefix)
                        .replace("{sender-suffix}", senderSuffix)
                        .replace("{sender-name}", senderName)
                        .replace("{recipient-name}", recipientName)
                        .replace("{recipient-prefix}", recipientPrefix)
                        .replace("{recipient-suffix}", recipientSuffix))
                        .replace("{message}", message)
        );

        MsgCommand.sendMessageToSpy(sender, recipient,
                recipientPrefix, recipientName, recipientSuffix,
                senderPrefix, senderName, senderSuffix,
                senderFormat, message,
                jsonStorage, configuration);
    }

}

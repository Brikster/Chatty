package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonPrimitive;
import net.amoebaman.util.ArrayWrapper;
import org.bukkit.Bukkit;
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
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.Arrays;

public class MsgCommand extends BukkitCommand {

    private final Configuration configuration;
    private final JsonStorage jsonStorage;

    private final PrefixAndSuffixManager prefixAndSuffixManager;
    private final ModerationManager moderationManager;

    public MsgCommand(
            Configuration configuration,
            DependencyManager dependencyManager,
            JsonStorage jsonStorage,
            ModerationManager moderationManager) {
        super("msg", ArrayWrapper.toArray(configuration.getNode("pm.commands.msg.aliases").getAsStringList(), String.class));

        this.configuration = configuration;
        this.jsonStorage = jsonStorage;

        this.prefixAndSuffixManager = new PrefixAndSuffixManager(dependencyManager, jsonStorage);
        this.moderationManager = moderationManager;
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

        boolean cancelledByModeration = false;
        String senderName, senderPrefix, senderSuffix;
        if (sender instanceof Player) {
            senderName = ((Player) sender).getDisplayName();
            senderPrefix = prefixAndSuffixManager.getPrefix((Player) sender);
            senderSuffix = prefixAndSuffixManager.getSuffix((Player) sender);
            jsonStorage.setProperty((Player) sender, "last-pm-interlocutor", new JsonPrimitive(recipientName));

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
                            Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(),
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
                            Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(),
                                    () -> sender.sendMessage(adsFound), 5L);
                    }
                }
            }
        } else {
            senderName = sender.getName();
            senderPrefix = "";
            senderSuffix = "";
        }

        if (cancelledByModeration) {
            return;
        }

        String senderFormat;
        if (!jsonStorage.isIgnore(recipient, sender)) {
            recipient.sendMessage(TextUtil.stylish(configuration.getNode("pm.format.recipient")
                    .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}")
                    .replace("{sender-prefix}", senderPrefix)
                    .replace("{sender-suffix}", senderSuffix)
                    .replace("{sender-name}", senderName)
                    .replace("{recipient-name}", recipientName)
                    .replace("{recipient-prefix}", recipientPrefix)
                    .replace("{recipient-suffix}", recipientSuffix))
                    .replace("{message}", message));
        }

        sender.sendMessage(senderFormat = TextUtil.stylish(configuration.getNode("pm.format.sender")
                .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}")
                .replace("{sender-prefix}", senderPrefix)
                .replace("{sender-suffix}", senderSuffix)
                .replace("{sender-name}", senderName)
                .replace("{recipient-name}", recipientName)
                .replace("{recipient-prefix}", recipientPrefix)
                .replace("{recipient-suffix}", recipientSuffix))
                .replace("{message}", message));

        MsgCommand.sendMessageToSpy(sender, recipient,
                recipientPrefix, recipientName, recipientSuffix,
                senderPrefix, senderName, senderSuffix,
                senderFormat, message,
                jsonStorage, configuration);
    }

    static void sendMessageToSpy(CommandSender sender, CommandSender recipient,
                                 String recipientPrefix, String recipientName, String recipientSuffix,
                                 String senderPrefix, String senderName, String senderSuffix,
                                 String senderFormat, String message,
                                 JsonStorage jsonStorage, Configuration configuration) {
        Reflection.getOnlinePlayers().stream()
                .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(recipient))
                .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
                .filter(spyPlayer -> jsonStorage.getProperty(spyPlayer, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean())
                .forEach(spyPlayer -> spyPlayer.sendMessage(
                        TextUtil.stylish(configuration.getNode("spy.format.pm")
                                .getAsString("&6[Spy] &r{format}")
                                .replace("{sender-prefix}", senderPrefix)
                                .replace("{sender-suffix}", senderSuffix)
                                .replace("{sender-name}", senderName)
                                .replace("{recipient-name}", recipientName)
                                .replace("{recipient-prefix}", recipientPrefix)
                                .replace("{recipient-suffix}", recipientSuffix))
                                .replace("{message}", message)
                                .replace("{format}", senderFormat)
                ));
    }

}

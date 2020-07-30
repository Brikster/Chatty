package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.dependencies.PlayerTagManager;
import ru.mrbrikster.chatty.moderation.AdvertisementModerationMethod;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.TextUtil;

public abstract class PrivateMessageCommand extends BukkitCommand {

    protected final Configuration configuration;
    protected final JsonStorage jsonStorage;

    private final PlayerTagManager playerTagManager;
    private final ModerationManager moderationManager;

    public PrivateMessageCommand(Chatty chatty, String name, String... aliases) {
        super(name, aliases);

        this.configuration = chatty.getExact(Configuration.class);
        this.jsonStorage = chatty.getExact(JsonStorage.class);

        this.playerTagManager = chatty.getExact(PlayerTagManager.class);
        this.moderationManager = chatty.getExact(ModerationManager.class);
    }

    protected void handlePrivateMessage(@NotNull CommandSender sender, @NotNull CommandSender recipient, @NotNull String message) {
        String recipientName = recipient.getName();
        String recipientPrefix = "";
        String recipientSuffix = "";

        if (recipient instanceof Player) {
            recipientName = ((Player) recipient).getDisplayName();
            recipientPrefix = playerTagManager.getPrefix((Player) recipient);
            recipientSuffix = playerTagManager.getSuffix((Player) recipient);
            jsonStorage.setProperty((Player) recipient, "last-pm-interlocutor", new JsonPrimitive(sender.getName()));
        }

        boolean cancelledByModeration = false;

        String senderName = sender.getName();
        String senderPrefix = "";
        String senderSuffix = "";

        if (sender instanceof Player) {
            senderName = ((Player) sender).getDisplayName();
            senderPrefix = playerTagManager.getPrefix((Player) sender);
            senderSuffix = playerTagManager.getSuffix((Player) sender);
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
        }

        if (cancelledByModeration) {
            return;
        }

        String senderFormat;
        if (!jsonStorage.isIgnore(recipient, sender)) {
            String recipientFormat = TextUtil.stylish(configuration.getNode("pm.format.recipient")
                    .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}")
                    .replace("{sender-prefix}", senderPrefix)
                    .replace("{sender-suffix}", senderSuffix)
                    .replace("{sender-name}", senderName)
                    .replace("{recipient-name}", recipientName)
                    .replace("{recipient-prefix}", recipientPrefix)
                    .replace("{recipient-suffix}", recipientSuffix))
                    .replace("{message}", message);

            if (!(recipient instanceof Player)) {
                recipientFormat = TextUtil.stripHex(recipientFormat);
            }

            recipient.sendMessage(recipientFormat);
        }

        senderFormat = TextUtil.stylish(configuration.getNode("pm.format.sender")
                .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}")
                .replace("{sender-prefix}", senderPrefix)
                .replace("{sender-suffix}", senderSuffix)
                .replace("{sender-name}", senderName)
                .replace("{recipient-name}", recipientName)
                .replace("{recipient-prefix}", recipientPrefix)
                .replace("{recipient-suffix}", recipientSuffix))
                .replace("{message}", message);

        if (!(sender instanceof Player)) {
            TextUtil.stripHex(senderFormat);
        }

        sender.sendMessage(senderFormat);

        String stylishedSpyMessage = TextUtil.stylish(configuration.getNode("spy.format.pm")
                .getAsString("&6[Spy] &r{format}")
                .replace("{sender-prefix}", senderPrefix)
                .replace("{sender-suffix}", senderSuffix)
                .replace("{sender-name}", senderName)
                .replace("{recipient-name}", recipientName)
                .replace("{recipient-prefix}", recipientPrefix)
                .replace("{recipient-suffix}", recipientSuffix))
                .replace("{message}", message)
                .replace("{format}", senderFormat);

        Reflection.getOnlinePlayers().stream()
                .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(recipient))
                .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
                .filter(spyPlayer -> jsonStorage.getProperty(spyPlayer, "spy-mode").orElse(new JsonPrimitive(true)).getAsBoolean())
                .forEach(spyPlayer -> spyPlayer.sendMessage(stylishedSpyMessage));
    }

}

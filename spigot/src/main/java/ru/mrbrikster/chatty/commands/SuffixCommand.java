package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.Arrays;

public class SuffixCommand extends BukkitCommand {

    private final Configuration configuration;
    private final JsonStorage jsonStorage;
    private final DependencyManager dependencyManager;

    SuffixCommand(Configuration configuration,
                  DependencyManager dependencyManager,
                  JsonStorage jsonStorage) {
        super("suffix", "setsuffix");

        this.configuration = configuration;
        this.dependencyManager = dependencyManager;
        this.jsonStorage = jsonStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("chatty.command.suffix")) {
                sender.sendMessage(Chatty.instance().messages().get("no-permission"));
                return;
            }

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(Chatty.instance().messages().get("suffix-command.player-not-found"));
                return;
            }

            if (!player.equals(sender) && !sender.hasPermission("chatty.command.suffix.others")) {
                sender.sendMessage(Chatty.instance().messages().get("suffix-command.no-permission-others"));
                return;
            }

            if (args[1].equalsIgnoreCase("clear")) {
                jsonStorage.setProperty(player, "suffix", null);

                if (configuration.getNode("miscellaneous.commands.suffix.auto-nte").getAsBoolean(false)) {
                    if (dependencyManager.getNametagEdit() != null) {
                        dependencyManager.getNametagEdit().setSuffix(player, null);
                    }
                }

                sender.sendMessage(Chatty.instance().messages().get("suffix-command.suffix-clear")
                        .replace("{player}", player.getName()));
            } else {
                String suffix = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                String formattedSuffix = configuration.getNode("miscellaneous.commands.suffix.before-suffix").getAsString("") + suffix;

                int minLimit = configuration.getNode("miscellaneous.commands.suffix.length-limit.min").getAsInt(3);
                int maxLimit = configuration.getNode("miscellaneous.commands.suffix.length-limit.max").getAsInt(16);
                if (formattedSuffix.length() > maxLimit) {
                    sender.sendMessage(Chatty.instance().messages().get("suffix-command.length-limit-max")
                            .replace("{limit}", String.valueOf(maxLimit - formattedSuffix.length() + suffix.length())));
                    return;
                }

                if (formattedSuffix.length() < minLimit) {
                    sender.sendMessage(Chatty.instance().messages().get("suffix-command.length-limit-min")
                            .replace("{limit}", String.valueOf(minLimit - formattedSuffix.length() + suffix.length())));
                    return;
                }

                jsonStorage.setProperty(player, "suffix", new JsonPrimitive(formattedSuffix));

                if (configuration.getNode("miscellaneous.commands.suffix.auto-nte").getAsBoolean(false)) {
                    if (dependencyManager.getNametagEdit() != null) {
                        dependencyManager.getNametagEdit().setSuffix(player, formattedSuffix);
                    }
                }

                sender.sendMessage(Chatty.instance().messages().get("suffix-command.suffix-set")
                        .replace("{player}", player.getName())
                        .replace("{suffix}", TextUtil.stylish(suffix)));
            }
        } else {
            sender.sendMessage(Chatty.instance().messages().get("suffix-command.usage")
                    .replace("{label}", label));
        }
    }

}

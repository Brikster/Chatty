package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.dependencies.DependencyManager;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PrefixCommand extends BukkitCommand {

    private final Configuration configuration;
    private final DependencyManager dependencyManager;
    private final PermanentStorage permanentStorage;

    PrefixCommand(Configuration configuration,
                  DependencyManager dependencyManager,
                  PermanentStorage permanentStorage) {
        super("prefix", "setprefix");

        this.configuration = configuration;
        this.dependencyManager = dependencyManager;
        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("chatty.command.prefix")) {
                sender.sendMessage(Chatty.instance().getMessages().get("no-permission"));
                return;
            }

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(Chatty.instance().getMessages().get("prefix-command.player-not-found"));
                return;
            }

            if (!player.equals(sender) && !sender.hasPermission("chatty.command.prefix.others")) {
                sender.sendMessage(Chatty.instance().getMessages().get("prefix-command.no-permission-others"));
                return;
            }

            if (args[1].equalsIgnoreCase("clear")) {
                permanentStorage.setProperty(player, "prefix", null);

                if (configuration.getNode("commands.prefix.auto-nte").getAsBoolean(false)) {
                    if (dependencyManager.getNametagEdit() != null) {
                        dependencyManager.getNametagEdit().setPrefix(player, null);
                    }
                }

                sender.sendMessage(Chatty.instance().getMessages().get("prefix-command.prefix-clear")
                        .replace("{player}", player.getName()));
            } else {
                String prefix = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.joining(" "));
                String formattedPrefix = prefix
                        + configuration.getNode("commands.prefix.after-prefix").getAsString("");

                permanentStorage.setProperty(player, "prefix", new JsonPrimitive(formattedPrefix));

                if (configuration.getNode("commands.prefix.auto-nte").getAsBoolean(false)) {
                    if (dependencyManager.getNametagEdit() != null) {
                        dependencyManager.getNametagEdit().setPrefix(player, formattedPrefix);
                    }
                }

                sender.sendMessage(Chatty.instance().getMessages().get("prefix-command.prefix-set")
                        .replace("{player}", player.getName())
                        .replace("{prefix}", ChatColor.translateAlternateColorCodes('&', prefix)));
            }
        } else {
            sender.sendMessage(Chatty.instance().getMessages().get("prefix-command.usage")
                    .replace("{label}", label));
        }
    }

}

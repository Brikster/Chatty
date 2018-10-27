package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.config.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SuffixCommand extends AbstractCommand {

    private final Configuration configuration;
    private final PermanentStorage permanentStorage;

    SuffixCommand(Configuration configuration,
                  PermanentStorage permanentStorage) {
        super("suffix", "setsuffix");

        this.configuration = configuration;
        this.permanentStorage = permanentStorage;
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("chatty.command.suffix")) {
                sender.sendMessage(Configuration.getMessages().get("no-permission"));
                return;
            }

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(Configuration.getMessages().get("suffix-command.player-not-found"));
                return;
            }

            if (!player.equals(sender) && !sender.hasPermission("chatty.command.suffix.others")) {
                sender.sendMessage(Configuration.getMessages().get("suffix-command.no-permission-others"));
                return;
            }

            if (args[1].equalsIgnoreCase("clear")) {
                permanentStorage.setProperty(player, "suffix", null);

                sender.sendMessage(Configuration.getMessages().get("suffix-command.suffix-clear")
                        .replace("{player}", player.getName()));
            } else {
                String suffix = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.joining(" "));

                permanentStorage.setProperty(player, "suffix", new JsonPrimitive(
                        configuration.getNode("general.suffix-command.before-suffix").getAsString("") + suffix));

                sender.sendMessage(Configuration.getMessages().get("suffix-command.suffix-set")
                        .replace("{player}", player.getName())
                        .replace("{suffix}", ChatColor.translateAlternateColorCodes('&', suffix)));
            }
        } else {
            sender.sendMessage(Configuration.getMessages().get("suffix-command.usage")
                    .replace("{label}", label));
        }
    }

}

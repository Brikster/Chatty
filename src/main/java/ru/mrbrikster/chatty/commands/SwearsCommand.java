package ru.mrbrikster.chatty.commands;

import com.google.common.io.Files;
import org.bukkit.command.CommandSender;
import ru.mrbrikster.chatty.config.Configuration;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class SwearsCommand extends AbstractCommand {

    SwearsCommand() {
        super("swears", "swear");
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("chatty.command.swears")) {
            if (args.length == 2
                    && args[0].equalsIgnoreCase("add")) {
                String word = args[1];

                try {
                    Files.append("\n" + word, SwearModerationMethod.getWhitelistFile(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                SwearModerationMethod.addWord(Pattern.compile(word, Pattern.CASE_INSENSITIVE));

                sender.sendMessage(Configuration.getMessages().get("swears-command.add-word").replace("{word}", word));
            } else sender.sendMessage(Configuration.getMessages().get("swears-command.usage")
                    .replace("{label}", label));
        } else sender.sendMessage(Configuration.getMessages().get("no-permission"));
    }
}

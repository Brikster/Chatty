package ru.mrbrikster.chatty.managers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.mrbrikster.chatty.Main;

public class CommandManager implements CommandExecutor {

    private final Main main;

    public CommandManager(Main main) {
        this.main = main;
        main.getCommand("chatty").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender.hasPermission("chatty.reload")) {
            main.init();
            commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("reload",
                    ChatColor.GREEN + "Config successful reloaded!"));
        } else {
            commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-permission",
                    ChatColor.RED + "You don''t have permission."));
        }

        return true;
    }

}

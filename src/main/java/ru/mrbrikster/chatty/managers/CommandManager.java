package ru.mrbrikster.chatty.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import ru.mrbrikster.chatty.Main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private final Main main;
    private boolean commandsRegistered;
    @Getter private List<Player> spyDisabledPlayers;

    public CommandManager(Main main) {
        this.main = main;
        this.spyDisabledPlayers = new ArrayList<>();
        main.getCommand("chatty").setExecutor(this);

        if (commandsRegistered)
            return;

        if (main.getConfiguration().isSpyEnabled()) {
            try {
                this.getCommandMap().register("spy", new Command("spy") {

                    @Override
                    public boolean execute(CommandSender commandSender, String label, String[] args) {
                        if (commandSender instanceof Player) {
                            if (!commandSender.hasPermission("chatty.command.spy")) {
                                commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-permission",
                                        ChatColor.RED + "You don't have permission."));
                                return true;
                            }

                            if (spyDisabledPlayers.contains(commandSender)) {
                                commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("spy-on",
                                        ChatColor.GREEN + "You have been enabled spy-mode."));
                                spyDisabledPlayers.remove(commandSender);
                            } else {
                                commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("spy-off",
                                        ChatColor.RED + "You have been disabled spy-mode."));
                                spyDisabledPlayers.add((Player) commandSender);
                            }
                        } else commandSender.sendMessage("Only for players.");
                        return true;
                    }

                });
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}

            this.commandsRegistered = true;
        }
    }

    private SimpleCommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        SimplePluginManager simplePluginManager = (SimplePluginManager) Bukkit.getServer()
                .getPluginManager();

        Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
        commandMapField.setAccessible(true);

        return (SimpleCommandMap) commandMapField.get(simplePluginManager);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender.hasPermission("chatty.command.reload")) {
            main.init();
            commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("reload",
                    ChatColor.GREEN + "Config successful reloaded!"));
        } else {
            commandSender.sendMessage(main.getConfiguration().getMessages().getOrDefault("no-permission",
                    ChatColor.RED + "You don't have permission."));
        }

        return true;
    }

}

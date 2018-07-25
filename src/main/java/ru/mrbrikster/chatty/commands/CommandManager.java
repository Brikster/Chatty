package ru.mrbrikster.chatty.commands;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import ru.mrbrikster.chatty.Chatty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {

    private final Chatty chatty;
    private boolean commandsRegistered;
    @Getter private List<Player> spyDisabledPlayers;

    private static SimpleCommandMap commandMap;

    static {
        SimplePluginManager simplePluginManager = (SimplePluginManager) Bukkit.getServer()
                .getPluginManager();

        Field commandMapField = null;
        try {
            commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(commandMapField).setAccessible(true);

        try {
            CommandManager.commandMap = (SimpleCommandMap) commandMapField.get(simplePluginManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public CommandManager(Chatty chatty) {
        this.chatty = chatty;
        this.spyDisabledPlayers = new ArrayList<>();

        if (commandsRegistered)
            return;

        //new AbstractCommand()

        /*if (chatty.getConfiguration().isSpyEnabled()) {
            getCommandMap().register("spy", new Command("spy") {

                @Override
                public boolean execute(CommandSender commandSender, String label, String[] args) {
                    if (commandSender instanceof Player) {
                        if (!commandSender.hasPermission("chatty.command.spy")) {
                            commandSender.sendMessage(chatty.getConfiguration().getMessages().getOrDefault("no-permission",
                                    ChatColor.RED + "You don't have permission."));
                            return true;
                        }

                        if (spyDisabledPlayers.contains(commandSender)) {
                            commandSender.sendMessage(chatty.getConfiguration().getMessages().getOrDefault("spy-on",
                                    ChatColor.GREEN + "You have been enabled spy-mode."));
                            spyDisabledPlayers.remove(commandSender);
                        } else {
                            commandSender.sendMessage(chatty.getConfiguration().getMessages().getOrDefault("spy-off",
                                    ChatColor.RED + "You have been disabled spy-mode."));
                            spyDisabledPlayers.add((Player) commandSender);
                        }
                    } else commandSender.sendMessage("Only for players.");
                    return true;
                }

            });

            this.commandsRegistered = true;
        } */
    }

    public static SimpleCommandMap getCommandMap() {
        return commandMap;
    }

}

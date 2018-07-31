package ru.mrbrikster.chatty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public abstract class AbstractCommand extends Command {

    AbstractCommand(String name, String... aliases) {
        super(name);
        setAliases(Arrays.asList(aliases));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        this.handle(sender, label, args);
        return true;
    }

    public abstract void handle(CommandSender sender, String label, String[] args);

    void registerCommand(CommandMap commandMap) {
        commandMap.register("chatty", this);
    }

    @SuppressWarnings("all")
    public void unregisterCommand(CommandMap commandMap) {
        this.unregister(commandMap);

        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);

            Map<String, Command> commands = (Map<String, Command>) field.get((SimpleCommandMap) commandMap);
            commands.remove(getLabel());
            commands.remove("chatty:" + getLabel());

            field.set((SimpleCommandMap) commandMap, commands);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}

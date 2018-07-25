package ru.mrbrikster.chatty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public abstract class AbstractCommand extends Command {

    public AbstractCommand(String name, String... aliases) {
        super(name);
        setAliases(Arrays.asList(aliases));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        this.handle(sender, label, args);
        return true;
    }

    public abstract void handle(CommandSender sender, String label, String[] args);

    public void registerCommand(CommandMap commandMap) {
        commandMap.register(getName(), this);
    }

}

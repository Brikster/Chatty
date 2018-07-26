package ru.mrbrikster.chatty.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.config.Configuration;

import java.lang.reflect.Field;
import java.util.Objects;

public class CommandManager {

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

    public CommandManager(Configuration configuration,
                          ChatManager chatManager) {
        new ChattyCommand(configuration)
                .registerCommand(getCommandMap());

        new SpyCommand(configuration,
                chatManager).register(getCommandMap());
    }

    private static SimpleCommandMap getCommandMap() {
        return commandMap;
    }

}

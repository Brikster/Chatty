package ru.mrbrikster.chatty.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.commands.pm.MessagesStorage;
import ru.mrbrikster.chatty.commands.pm.MsgCommand;
import ru.mrbrikster.chatty.commands.pm.ReplyCommand;
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

    private final ChattyCommand chattyCommand;
    private final SpyCommand spyCommand;
    private final MsgCommand msgCommand;
    private final ReplyCommand replyCommand;

    public CommandManager(Configuration configuration,
                          ChatManager chatManager) {
        this.chattyCommand = new ChattyCommand(configuration);
        this.spyCommand = new SpyCommand(chatManager);

        MessagesStorage messagesStorage = new MessagesStorage();
        this.msgCommand = new MsgCommand(messagesStorage);
        this.replyCommand = new ReplyCommand(messagesStorage);

        this.chattyCommand.registerCommand(getCommandMap());
        this.spyCommand.registerCommand(getCommandMap());
        this.msgCommand.registerCommand(getCommandMap());
        this.replyCommand.registerCommand(getCommandMap());
    }

    private static SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    public void unregisterAll() {
        this.chattyCommand.unregisterCommand(getCommandMap());
        this.spyCommand.unregisterCommand(getCommandMap());
        this.msgCommand.unregisterCommand(getCommandMap());
        this.replyCommand.unregisterCommand(getCommandMap());
    }

}

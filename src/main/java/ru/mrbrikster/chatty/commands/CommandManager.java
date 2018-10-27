package ru.mrbrikster.chatty.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.commands.pm.IgnoreCommand;
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
    private final ClearChatCommand clearChatCommand;
    private final SpyCommand spyCommand;
    private SwearsCommand swearsCommand;
    private IgnoreCommand ignoreCommand;
    private MsgCommand msgCommand;
    private ReplyCommand replyCommand;

    public CommandManager(Configuration configuration,
                          TemporaryStorage temporaryStorage,
                          PermanentStorage permanentStorage) {
        this.chattyCommand = new ChattyCommand(configuration);
        this.clearChatCommand = new ClearChatCommand();
        this.spyCommand = new SpyCommand(temporaryStorage);

        this.chattyCommand.registerCommand(getCommandMap());
        this.clearChatCommand.registerCommand(getCommandMap());
        this.spyCommand.registerCommand(getCommandMap());

        if (configuration.getNode("general.pm").getAsBoolean(false)) {
            this.msgCommand = new MsgCommand(configuration, temporaryStorage, permanentStorage);
            this.replyCommand = new ReplyCommand(configuration, temporaryStorage, permanentStorage);
            this.ignoreCommand = new IgnoreCommand(permanentStorage);

            this.msgCommand.registerCommand(getCommandMap());
            this.ignoreCommand.registerCommand(getCommandMap());
            this.replyCommand.registerCommand(getCommandMap());
        }

        if (configuration.getNode("moderation.swear.enable").getAsBoolean(false)) {
            this.swearsCommand = new SwearsCommand();
            this.swearsCommand.registerCommand(getCommandMap());
        }
    }

    private static SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    public void unregisterAll() {
        this.chattyCommand.unregisterCommand(getCommandMap());
        this.clearChatCommand.unregisterCommand(getCommandMap());
        this.spyCommand.unregisterCommand(getCommandMap());

        if (msgCommand != null)
            this.msgCommand.unregisterCommand(getCommandMap());

        if (ignoreCommand != null)
            this.ignoreCommand.unregisterCommand(getCommandMap());

        if (replyCommand != null)
            this.replyCommand.unregisterCommand(getCommandMap());

        if (swearsCommand != null)
            this.swearsCommand.unregisterCommand(getCommandMap());
    }

}

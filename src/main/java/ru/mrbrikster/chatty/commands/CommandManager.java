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
import ru.mrbrikster.chatty.dependencies.DependencyManager;

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

    private final Configuration configuration;
    private final TemporaryStorage temporaryStorage;
    private final PermanentStorage permanentStorage;
    private final DependencyManager dependencyManager;

    private ChattyCommand chattyCommand;
    private ClearChatCommand clearChatCommand;
    private SpyCommand spyCommand;
    private IgnoreCommand ignoreCommand;
    private MsgCommand msgCommand;
    private ReplyCommand replyCommand;
    private SwearsCommand swearsCommand;
    private PrefixCommand prefixCommand;
    private SuffixCommand suffixCommand;

    public CommandManager(Configuration configuration,
                          DependencyManager dependencyManager,
                          TemporaryStorage temporaryStorage,
                          PermanentStorage permanentStorage) {
        this.configuration = configuration;
        this.dependencyManager = dependencyManager;
        this.temporaryStorage = temporaryStorage;
        this.permanentStorage = permanentStorage;

        this.init();

        configuration.registerReloadHandler(() -> {
            this.unregisterAll();
            this.init();
        });
    }

    private void init() {
        this.chattyCommand = new ChattyCommand(configuration);
        this.clearChatCommand = new ClearChatCommand();
        this.spyCommand = new SpyCommand(temporaryStorage);

        this.chattyCommand.registerCommand(getCommandMap());
        this.clearChatCommand.registerCommand(getCommandMap());
        this.spyCommand.registerCommand(getCommandMap());

        if (configuration.getNode("commands.msg.enable").getAsBoolean(false)) {
            this.msgCommand = new MsgCommand(configuration, temporaryStorage, permanentStorage);
            this.msgCommand.registerCommand(getCommandMap());
        }

        if (configuration.getNode("commands.ignore.enable").getAsBoolean(false)) {
            this.ignoreCommand = new IgnoreCommand(configuration, permanentStorage);
            this.ignoreCommand.registerCommand(getCommandMap());
        }

        if (configuration.getNode("commands.reply.enable").getAsBoolean(false)) {
            this.replyCommand = new ReplyCommand(configuration, temporaryStorage, permanentStorage);
            this.replyCommand.registerCommand(getCommandMap());
        }

        if (configuration.getNode("moderation.swear.enable").getAsBoolean(false)) {
            this.swearsCommand = new SwearsCommand();
            this.swearsCommand.registerCommand(getCommandMap());
        }

        if (configuration.getNode("general.prefix-command.enable").getAsBoolean(false)) {
            this.prefixCommand = new PrefixCommand(configuration, dependencyManager, permanentStorage);
            this.prefixCommand.registerCommand(getCommandMap());
        }

        if (configuration.getNode("general.suffix-command.enable").getAsBoolean(false)) {
            this.suffixCommand = new SuffixCommand(configuration, dependencyManager, permanentStorage);
            this.suffixCommand.registerCommand(getCommandMap());
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

        if (prefixCommand != null)
            this.prefixCommand.unregisterCommand(getCommandMap());

        if (suffixCommand != null)
            this.suffixCommand.unregisterCommand(getCommandMap());
    }

}

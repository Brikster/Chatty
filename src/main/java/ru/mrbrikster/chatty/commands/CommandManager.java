package ru.mrbrikster.chatty.commands;

import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.PermanentStorage;
import ru.mrbrikster.chatty.chat.TemporaryStorage;
import ru.mrbrikster.chatty.commands.pm.IgnoreCommand;
import ru.mrbrikster.chatty.commands.pm.MsgCommand;
import ru.mrbrikster.chatty.commands.pm.ReplyCommand;
import ru.mrbrikster.chatty.dependencies.DependencyManager;

public class CommandManager {

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

        this.chattyCommand.register(Chatty.instance());
        this.clearChatCommand.register(Chatty.instance());
        this.spyCommand.register(Chatty.instance());

        if (configuration.getNode("commands.msg.enable").getAsBoolean(false)) {
            this.msgCommand = new MsgCommand(configuration, temporaryStorage, permanentStorage);
            this.msgCommand.register(Chatty.instance());
        }

        if (configuration.getNode("commands.ignore.enable").getAsBoolean(false)) {
            this.ignoreCommand = new IgnoreCommand(configuration, permanentStorage);
            this.ignoreCommand.register(Chatty.instance());
        }

        if (configuration.getNode("commands.reply.enable").getAsBoolean(false)) {
            this.replyCommand = new ReplyCommand(configuration, temporaryStorage, permanentStorage);
            this.replyCommand.register(Chatty.instance());
        }

        if (configuration.getNode("moderation.swear.enable").getAsBoolean(false)) {
            this.swearsCommand = new SwearsCommand();
            this.swearsCommand.register(Chatty.instance());
        }

        if (configuration.getNode("commands.prefix.enable").getAsBoolean(false)) {
            this.prefixCommand = new PrefixCommand(configuration, dependencyManager, permanentStorage);
            this.prefixCommand.register(Chatty.instance());
        }

        if (configuration.getNode("commands.suffix.enable").getAsBoolean(false)) {
            this.suffixCommand = new SuffixCommand(configuration, dependencyManager, permanentStorage);
            this.suffixCommand.register(Chatty.instance());
        }
    }

    public void unregisterAll() {
        this.chattyCommand.unregister(Chatty.instance());
        this.clearChatCommand.unregister(Chatty.instance());
        this.spyCommand.unregister(Chatty.instance());

        if (msgCommand != null)
            this.msgCommand.unregister(Chatty.instance());

        if (ignoreCommand != null)
            this.ignoreCommand.unregister(Chatty.instance());

        if (replyCommand != null)
            this.replyCommand.unregister(Chatty.instance());

        if (swearsCommand != null)
            this.swearsCommand.unregister(Chatty.instance());

        if (prefixCommand != null)
            this.prefixCommand.unregister(Chatty.instance());

        if (suffixCommand != null)
            this.suffixCommand.unregister(Chatty.instance());
    }

}

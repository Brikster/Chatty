package ru.brikster.chatty.command.handler;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public final class ClearChatCommandHandler implements CommandExecutionHandler<CommandSender> {

    private static final String ALL_SCOPE = "all";
    private static final String ALL_PERMISSION = "chatty.command.clearchat.all";
    private static final int BLANK_LINES = 100;

    private final BukkitAudiences audiences;
    private final MessagesConfig messagesConfig;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        CommandSender sender = commandContext.getSender();
        Optional<String> scope = commandContext.getOptional(ALL_SCOPE);

        if (scope.isEmpty()) {
            clearForSender(sender);
            return;
        }

        if (!scope.get().equalsIgnoreCase(ALL_SCOPE)) {
            audiences.sender(sender).sendMessage(messagesConfig.getCmdUsageError()
                    .replaceText(AdventureUtil.createReplacement("{usage}", "/clearchat [all]")));
            return;
        }

        if (!sender.hasPermission(ALL_PERMISSION)) {
            audiences.sender(sender).sendMessage(messagesConfig.getCmdNoPermissionError());
            return;
        }

        clearForEveryone(sender);
    }

    private void clearForSender(CommandSender sender) {
        if (!(sender instanceof Player)) {
            audiences.sender(sender).sendMessage(messagesConfig.getCmdSenderTypeError());
            return;
        }
        var audience = audiences.sender(sender);
        for (int i = 0; i < BLANK_LINES; i++) {
            audience.sendMessage(Component.empty());
        }
        audience.sendMessage(messagesConfig.getClearchatCommandSuccess());
    }

    private void clearForEveryone(CommandSender sender) {
        var players = audiences.filter(candidate -> !(candidate instanceof ConsoleCommandSender));
        for (int i = 0; i < BLANK_LINES; i++) {
            players.sendMessage(Component.empty());
        }
        players.sendMessage(messagesConfig.getClearchatCommandClearedForAll()
                .replaceText(AdventureUtil.createReplacement("{player}", sender.getName())));
    }

}

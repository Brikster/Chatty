package ru.brikster.chatty.command.handler;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public final class BroadcastCommandHandler implements CommandExecutionHandler<CommandSender> {

    private final Plugin plugin;
    private final ChatRegistry chatRegistry;
    private final ComponentStringConverter componentStringConverter;
    private final MessagesConfig messagesConfig;
    private final BukkitAudiences audiences;

    @Override
    public void execute(@NonNull CommandContext<CommandSender> commandContext) {
        String chatId = commandContext.get("chat");
        String message = commandContext.get("message");

        Chat chat = chatRegistry.getChats().get(chatId);
        if (chat == null) {
            audiences.sender(commandContext.getSender()).sendMessage(messagesConfig.getChatNotFound());
            return;
        }

        Component component = componentStringConverter.stringToComponent(message);
        chat.sendJsonComponent(plugin, GsonComponentSerializer.gson().serialize(component));
    }

}

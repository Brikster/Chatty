package ru.brikster.chatty.chat.message.transform.decorations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

@Singleton
public final class PlayerDecorationsFormatter {

    public @NotNull Component formatMessageWithDecorations(@NotNull CommandSender sender, @NotNull String message) {
        return sender.hasPermission("chatty.decorations")
                ? LegacyComponentSerializer.legacyAmpersand().deserialize(message)
                : Component.text(message);
    }

}

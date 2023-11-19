package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.command.CommandSuggestionsProvider;
import ru.brikster.chatty.config.type.PmConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class PrivateMessageSuggestionsProvider implements CommandSuggestionsProvider<CommandSender> {

    @Inject private PmConfig pmConfig;

    @Override
    public @NotNull List<@NotNull String> provideSuggestions(@NotNull CommandContext<@NotNull CommandSender> commandContext, @NotNull String arg) {

        List<String> suggestions = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                suggestions.add(onlinePlayer.getName());
            }
        }
        if (pmConfig.isAllowConsole()) {
            suggestions.add("Console");
        }
        return suggestions;
    }

}

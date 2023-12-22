package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.command.CommandSuggestionsProvider;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.proxy.ProxyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public final class PrivateMessageSuggestionsProvider implements CommandSuggestionsProvider<CommandSender> {

    @Inject private PmConfig pmConfig;
    @Inject private ProxyService proxyService;

    @Override
    public @NotNull List<@NotNull String> provideSuggestions(@NotNull CommandContext<@NotNull CommandSender> commandContext, @NotNull String arg) {
        Set<String> suggestions = new HashSet<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                suggestions.add(onlinePlayer.getName());
            }
        }
        for (String proxyPlayerName : proxyService.getOnlinePlayers()) {
            if (proxyPlayerName.toLowerCase().startsWith(arg.toLowerCase())) {
                suggestions.add(proxyPlayerName);
            }
        }
        if (pmConfig.isAllowConsole()) {
            suggestions.add("Console");
        }
        return new ArrayList<>(suggestions);
    }

}

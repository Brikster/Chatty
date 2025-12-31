package ru.brikster.chatty.pm;

import cloud.commandframework.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.command.CommandSuggestionsProvider;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.proxy.ProxyService;
import ru.brikster.chatty.util.EventUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public final class PrivateMessageSuggestionsProvider implements CommandSuggestionsProvider<CommandSender> {

    @Inject private PmConfig pmConfig;
    @Inject private ProxyService proxyService;
    @Inject private Plugin plugin;

    @Override
    public @NotNull List<@NotNull String> provideSuggestions(@NotNull CommandContext<@NotNull CommandSender> commandContext, @NotNull String arg) {
        if (!Bukkit.isPrimaryThread()) {
            return EventUtil.callSynchronously(plugin, () -> buildSuggestions(commandContext, arg));
        }
        return buildSuggestions(commandContext, arg);
    }

    private @NotNull List<@NotNull String> buildSuggestions(@NotNull CommandContext<@NotNull CommandSender> commandContext,
                                                            @NotNull String arg) {
        Player senderPlayer = commandContext.getSender() instanceof Player
                ? (Player) commandContext.getSender()
                : null;

        String lowerArg = arg.toLowerCase(Locale.ROOT);
        Set<String> suggestions = new HashSet<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase(Locale.ROOT).startsWith(lowerArg)) {
                if (senderPlayer != null && !senderPlayer.canSee(onlinePlayer)) {
                    continue;
                }
                suggestions.add(onlinePlayer.getName());
            }
        }
        for (String proxyPlayerName : proxyService.getOnlinePlayers()) {
            if (proxyPlayerName.toLowerCase(Locale.ROOT).startsWith(lowerArg)) {
                suggestions.add(proxyPlayerName);
            }
        }
        if (pmConfig.isAllowConsole()) {
            suggestions.add("Console");
        }
        return new ArrayList<>(suggestions);
    }

}

package ru.brikster.chatty.pm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.context.TwoPlayersTransformContext;
import ru.brikster.chatty.chat.component.impl.LinkParserComponentTransformer;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.pm.placeholders.PmFromPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.pm.placeholders.PmToPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.pm.prefix.PmFromPrefixComponentTransformer;
import ru.brikster.chatty.chat.component.impl.pm.prefix.PmToPrefixComponentTransformer;
import ru.brikster.chatty.chat.message.transform.decorations.PlayerDecorationsFormatter;
import ru.brikster.chatty.config.file.PmConfig;
import ru.brikster.chatty.config.file.ProxyConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.pm.targets.CommandSenderPmMessageTarget;
import ru.brikster.chatty.pm.targets.PmMessageTarget;
import ru.brikster.chatty.pm.targets.RemotePmMessageTarget;
import ru.brikster.chatty.proxy.ProxyService;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Singleton
public class PmMessageService {

    @Inject private PmConfig pmConfig;
    @Inject private PmFromPrefixComponentTransformer pmFromPrefixComponentTransformer;
    @Inject private PmToPrefixComponentTransformer pmToPrefixComponentTransformer;
    @Inject private PmFromPlaceholdersComponentTransformer pmFromPlaceholdersTransformer;
    @Inject private PmToPlaceholdersComponentTransformer pmToPlaceholdersTransformer;
    @Inject private RelationalPlaceholdersComponentTransformer relationalPlaceholdersTransformer;
    @Inject private PlayerDecorationsFormatter decorationsFormatter;
    @Inject private LinkParserComponentTransformer linkParserComponentTransformer;
    @Inject private ComponentStringConverter componentStringConverter;
    @Inject private ProxyConfig proxyConfig;
    @Inject private ProxyService proxyService;

    private final Cache<String, String> lastConversations = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    public @Nullable PmMessageTarget resolveTarget(String targetName, boolean allowConsole) {
        PmMessageTarget target = null;
        if (allowConsole && pmConfig.isAllowConsole() && targetName.equalsIgnoreCase("Console")) {
            target = new CommandSenderPmMessageTarget(Bukkit.getConsoleSender());
        } else {
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                target = new CommandSenderPmMessageTarget(targetPlayer);
            }
        }

        if (target == null) {
            for (String onlinePlayerName : proxyService.getOnlinePlayers()) {
                if (onlinePlayerName.equalsIgnoreCase(targetName)) {
                    return new RemotePmMessageTarget(onlinePlayerName, proxyService.getUuidByUsername(onlinePlayerName));
                }
            }
        }

        return target;
    }

    public @NotNull Component transformFormat(@NotNull String formatString,
                                              @NotNull CommandSender sender,
                                              @NotNull PmMessageTarget target,
                                              @NotNull String message) {
        Component formatComponent = componentStringConverter.stringToComponent(formatString);
        formatComponent = formatFromPlaceholders(formatComponent, sender);
        formatComponent = formatToPlaceholders(formatComponent, target);
        if (target.isOnline()) {
            formatComponent = formatRelationalPlaceholders(formatComponent, sender, target.asCommandSender());
        }
        formatComponent = formatWithMessage(formatComponent, sender, message);
        return formatComponent;
    }

    public @NotNull Component formatWithMessage(@NotNull Component component, @NotNull CommandSender sender, @NotNull String message) {
        Component messageComponent = decorationsFormatter.formatMessageWithDecorations(sender, message);
        if (pmConfig.isParseLinks()) {
            messageComponent = linkParserComponentTransformer.transform(messageComponent,
                    SinglePlayerTransformContext.of((Player) sender));
        }
        return component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("{message}")
                .replacement(messageComponent)
                .build());
    }

    public @NotNull Component formatFromPlaceholders(@NotNull Component component, @NotNull CommandSender sender) {
        Component updatedComponent;
        if (sender instanceof ConsoleCommandSender) {
            updatedComponent = component
                    .replaceText(AdventureUtil.createReplacement("{from-prefix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{from-suffix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{from-name}", sender.getName()));
        } else {
            updatedComponent = pmFromPrefixComponentTransformer.transform(component, SinglePlayerTransformContext.of((OfflinePlayer) sender));
            updatedComponent = pmFromPlaceholdersTransformer.transform(updatedComponent, SinglePlayerTransformContext.of((OfflinePlayer) sender));
            updatedComponent = updatedComponent
                    .replaceText(AdventureUtil.createReplacement("{from-name}", sender.getName()));
        }
        return updatedComponent;
    }

    public @NotNull Component formatToPlaceholders(@NotNull Component component, @NotNull PmMessageTarget messageTarget) {
        Component updatedComponent;
        if (messageTarget.isConsole()) {
            updatedComponent = component
                    .replaceText(AdventureUtil.createReplacement("{to-prefix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{to-suffix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{to-name}", messageTarget.getName()));
        } else {
            updatedComponent = pmToPrefixComponentTransformer.transform(component, SinglePlayerTransformContext.of(messageTarget.asOfflinePlayer()));
            updatedComponent = pmToPlaceholdersTransformer.transform(updatedComponent, SinglePlayerTransformContext.of(messageTarget.asOfflinePlayer()));
            updatedComponent = updatedComponent
                    .replaceText(AdventureUtil.createReplacement("{to-name}", messageTarget.getName()));
        }
        return updatedComponent;
    }

    public @NotNull Component formatRelationalPlaceholders(@NotNull Component component,
                                                           @NotNull CommandSender fromSender,
                                                           @NotNull CommandSender targetSender) {
        if (fromSender instanceof Player && targetSender instanceof Player) {
            return relationalPlaceholdersTransformer.transform(component,
                    TwoPlayersTransformContext.of((Player) fromSender, (Player) targetSender));
        } else {
            return component;
        }
    }

    public @Nullable PmMessageTarget getLastConversation(@NotNull CommandSender sender) {
        String targetName;
        if (proxyConfig.isEnable()) {
            targetName = proxyService.getLastConversation(sender.getName());
        } else {
            targetName = lastConversations.getIfPresent(sender.getName());
        }

        if (targetName == null) {
            return null;
        }

        CommandSender target;
        if (targetName.equals("Console")) {
            target = Bukkit.getConsoleSender();
        } else {
            target = Bukkit.getPlayerExact(targetName);
        }

        if (target == null && proxyConfig.isEnable()) {
            if (proxyService.isOnline(targetName)) {
                return new RemotePmMessageTarget(targetName, proxyService.getUuidByUsername(targetName));
            }
        }

        return target == null ? null : new CommandSenderPmMessageTarget(target);
    }

    public void addConversation(@NotNull String firstSender, @NotNull String secondSender) {
        if (proxyConfig.isEnable()) {
            proxyService.addConversation(firstSender, secondSender);
        } else {
            lastConversations.put(firstSender, secondSender);
        }
    }

}

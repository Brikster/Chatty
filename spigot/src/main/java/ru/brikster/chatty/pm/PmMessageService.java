package ru.brikster.chatty.pm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
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
import ru.brikster.chatty.config.type.MessagesConfig;
import ru.brikster.chatty.config.type.PmConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
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
    @Inject private MessagesConfig messagesConfig;
    @Inject private BukkitAudiences audiences;

    private final Cache<CommandSender, CommandSender> lastConversations = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .weakKeys()
            .weakValues()
            .build();

    public @Nullable CommandSender resolveTarget(String targetName, boolean allowConsole) {
        CommandSender target;
        if (allowConsole && pmConfig.isAllowConsole() && targetName.equalsIgnoreCase("Console")) {
            return Bukkit.getConsoleSender();
        } else {
            target = Bukkit.getPlayer(targetName);
            return target;
        }
    }

    public @NotNull Component transformFormat(@NotNull String formatString,
                                              @NotNull CommandSender fromSender,
                                              @NotNull CommandSender toSender,
                                              @NotNull String message) {
        Component formatComponent = componentStringConverter.stringToComponent(formatString);
        formatComponent = formatFromPlaceholders(formatComponent, fromSender);
        formatComponent = fromToPlaceholders(formatComponent, toSender);
        formatComponent = formatRelationalPlaceholders(formatComponent, fromSender, toSender);
        formatComponent = formatWithMessage(formatComponent, fromSender, message);
        return formatComponent;
    }

    public @NotNull Component formatWithMessage(@NotNull Component component, @NotNull CommandSender fromSender, @NotNull String message) {
        Component messageComponent = decorationsFormatter.formatMessageWithDecorations(fromSender, message);
        if (pmConfig.isParseLinks()) {
            messageComponent = linkParserComponentTransformer.transform(messageComponent,
                    SinglePlayerTransformContext.of((Player) fromSender));
        }
        return component.replaceText(TextReplacementConfig.builder()
                .matchLiteral("{message}")
                .replacement(messageComponent)
                .build());
    }

    public @NotNull Component formatFromPlaceholders(@NotNull Component component, @NotNull CommandSender fromSender) {
        Component updatedComponent;
        if (fromSender instanceof ConsoleCommandSender) {
            updatedComponent = component
                    .replaceText(AdventureUtil.createReplacement("{from-prefix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{from-suffix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{from-name}", fromSender.getName()));
        } else {
            updatedComponent = pmFromPrefixComponentTransformer.transform(component, SinglePlayerTransformContext.of((Player) fromSender));
            updatedComponent = pmFromPlaceholdersTransformer.transform(updatedComponent, SinglePlayerTransformContext.of((Player) fromSender));
            updatedComponent = updatedComponent
                    .replaceText(AdventureUtil.createReplacement("{from-name}", ((Player) fromSender).getDisplayName()));
        }
        return updatedComponent;
    }

    public @NotNull Component fromToPlaceholders(@NotNull Component component, @NotNull CommandSender fromSender) {
        Component updatedComponent;
        if (fromSender instanceof ConsoleCommandSender) {
            updatedComponent = component
                    .replaceText(AdventureUtil.createReplacement("{to-prefix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{to-suffix}", ""))
                    .replaceText(AdventureUtil.createReplacement("{to-name}", fromSender.getName()));
        } else {
            updatedComponent = pmToPrefixComponentTransformer.transform(component, SinglePlayerTransformContext.of((Player) fromSender));
            updatedComponent = pmToPlaceholdersTransformer.transform(updatedComponent, SinglePlayerTransformContext.of((Player) fromSender));
            updatedComponent = updatedComponent
                    .replaceText(AdventureUtil.createReplacement("{to-name}", ((Player) fromSender).getDisplayName()));
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

    public @Nullable CommandSender getLastConversation(@NotNull CommandSender sender) {
        return lastConversations.getIfPresent(sender);
    }

    public void addConversation(@NotNull CommandSender firstSender, @NotNull CommandSender secondSender) {
        lastConversations.put(firstSender, secondSender);
    }

}

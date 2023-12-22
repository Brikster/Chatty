package ru.brikster.chatty.chat.message.transform.stage.post;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.context.TwoPlayersTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.config.file.SettingsConfig.RelationalPlaceholdersOrder;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class MentionsTransformStrategy implements MessageTransformStrategy<Component> {

    @Inject private SettingsConfig settingsConfig;
    @Inject private ComponentStringConverter componentStringConverter;
    @Inject private RelationalPlaceholdersComponentTransformer relationalPlaceholdersComponentTransformer;
    @Inject private PlaceholdersComponentTransformer placeholdersComponentTransformer;
    @Inject private BukkitAudiences audiences;

    private final Cache<Player, Pattern> playerPatternCache = CacheBuilder.newBuilder()
            .weakKeys()
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    @Override
    public @NotNull MessageTransformResult<Component> handle(MessageContext<Component> context) {
        if (!context.getSender().hasPermission("chatty.mentions")) {
            return MessageTransformResultBuilder.<Component>fromContext(context).build();
        }

        PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();

        List<Player> mentionedPlayers = new ArrayList<>();

        Component message = context.getMessage();
        String plainTextMessage = plainTextComponentSerializer.serialize(message);

        // TODO optimize
        //noinspection unchecked
        for (Player onlinePlayer : ((Collection<? extends Player>) context.getMetadata().get("all_recipients"))) {
            // Cannot mention yourself
            if (onlinePlayer == context.getSender()) {
                continue;
            }

            Pattern pattern = patternForPlayer(onlinePlayer);

            Matcher matcher = pattern.matcher(plainTextMessage);
            if (!matcher.matches() && !matcher.find()) {
                continue;
            }

            boolean mentionTarget = onlinePlayer == context.getTarget();

            String format = mentionTarget
                    ? settingsConfig.getMentions().getTargetFormat()
                    : settingsConfig.getMentions().getOthersFormat();

            Component mentionFormatComponent = componentStringConverter.stringToComponent(format
                    .replace("{username}", onlinePlayer.getDisplayName()));

            if (!mentionTarget) {
                mentionFormatComponent = relationalPlaceholdersComponentTransformer.transform(mentionFormatComponent,
                        settingsConfig.getRelationalPlaceholdersOrder() == RelationalPlaceholdersOrder.SENDER_AND_TARGET
                            ? TwoPlayersTransformContext.of(onlinePlayer, context.getTarget())
                            : TwoPlayersTransformContext.of(context.getTarget(), onlinePlayer));
            }

            mentionFormatComponent = placeholdersComponentTransformer.transform(mentionFormatComponent,
                    SinglePlayerTransformContext.of(onlinePlayer));

            Component updatedMessage = context.getMessage()
                    .replaceText(TextReplacementConfig.builder()
                            .match(pattern)
                            .replacement(mentionFormatComponent)
                            .build());

            if (!updatedMessage.compact().equals(message.compact())) {
                message = updatedMessage;
                plainTextMessage = plainTextComponentSerializer.serialize(message);
                mentionedPlayers.add(onlinePlayer);
            }
        }

        if (settingsConfig.getMentions().isPlaySound()) {
            for (Player mentionedPlayer : mentionedPlayers) {
                if (mentionedPlayer == context.getTarget()) {
                    audiences.player(mentionedPlayer)
                            .playSound(settingsConfig.getMentions().getSound());
                }
            }
        }

        return MessageTransformResultBuilder.<Component>fromContext(context)
                .withMessage(message)
                .build();
    }

    private Pattern patternForPlayer(Player player) {
        try {
            return playerPatternCache.get(player, () ->
                    Pattern.compile(settingsConfig.getMentions().getPattern().replace("{username}",
                            player.getDisplayName())));
        } catch (ExecutionException e) {
            throw new IllegalStateException("Cannot compile mention pattern", e);
        }
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.POST;
    }

}

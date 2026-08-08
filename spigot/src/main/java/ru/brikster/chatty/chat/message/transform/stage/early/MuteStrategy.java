package ru.brikster.chatty.chat.message.transform.stage.early;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.config.file.MessagesConfig;
import ru.brikster.chatty.repository.player.Mute;
import ru.brikster.chatty.repository.player.PlayerDataRepository;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.MuteFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class MuteStrategy implements MessageTransformStrategy<String> {

    @Inject private PlayerDataRepository repository;
    @Inject private MessagesConfig messages;
    @Inject private BukkitAudiences audiences;

    @Override
    public @NotNull MessageTransformResult<String> handle(MessageContext<String> context) {
        if (context.getSender().hasPermission("chatty.bypass.mute")) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        Mute mute;
        try {
            mute = repository.getMute(context.getSender().getUniqueId());
        } catch (RuntimeException e) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        if (mute == null) {
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        if (mute.isExpired(System.currentTimeMillis())) {
            repository.clearMute(context.getSender().getUniqueId());
            return MessageTransformResultBuilder.<String>fromContext(context).build();
        }

        audiences.player(context.getSender()).sendMessage(messages.getMuted()
                .replaceText(AdventureUtil.createReplacement("{duration}",
                        MuteFormatter.describe(mute)))
                .replaceText(AdventureUtil.createReplacement("{reason}",
                        mute.getReason() == null ? "" : mute.getReason())));

        return MessageTransformResultBuilder.<String>fromContext(context)
                .withCancelled()
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

}

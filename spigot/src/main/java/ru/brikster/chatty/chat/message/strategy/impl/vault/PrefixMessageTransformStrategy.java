package ru.brikster.chatty.chat.message.strategy.impl.vault;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageTransformStrategy;
import ru.brikster.chatty.chat.message.context.MessageContextImpl;
import ru.brikster.chatty.chat.message.strategy.result.ResultImpl;
import ru.brikster.chatty.prefix.PrefixProvider;

import javax.inject.Inject;

public class PrefixMessageTransformStrategy implements MessageTransformStrategy<String, String> {

    private static final PrefixMessageTransformStrategy INSTANCE = new PrefixMessageTransformStrategy();

    private PrefixMessageTransformStrategy() {}

    @Inject
    private PrefixProvider prefixProvider;

    @Override
    public @NotNull Result<String> handle(MessageContext<String> context) {
        String prefix = prefixProvider.getPrefix(context.getSender());
        String suffix = prefixProvider.getSuffix(context.getSender());

        Component newFormat = context.getFormat();

        if (prefix != null) {
            newFormat = newFormat.replaceText(TextReplacementConfig.builder()
                    .match("<prefix>")
                    .replacement(prefix)
                    .build());
        }

        if (suffix != null) {
            newFormat = newFormat.replaceText(TextReplacementConfig.builder()
                    .match("<suffix>")
                    .replacement(suffix)
                    .build());
        }

        MessageContext<String> newContext = new MessageContextImpl<>(context);
        newContext.setFormat(newFormat);

        return ResultImpl.<String>builder()
                .newContext(newContext)
                .formatUpdated(newFormat.equals(context.getFormat()))
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.EARLY;
    }

    public static PrefixMessageTransformStrategy instance() {
        return INSTANCE;
    }

}

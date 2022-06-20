package ru.brikster.chatty.chat.handle.strategy.impl.vault;

import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;
import ru.brikster.chatty.api.chat.handle.strategy.MessageHandleStrategy;
import ru.brikster.chatty.chat.handle.context.MessageContextImpl;
import ru.brikster.chatty.chat.handle.strategy.result.ResultImpl;
import ru.brikster.chatty.prefix.PrefixProvider;

public class PrefixMessageHandleStrategy implements MessageHandleStrategy<String, String> {

    @Inject
    private PrefixProvider prefixProvider;

    @Override
    public Result<String> handle(MessageContext<String> context) {
        String prefix = prefixProvider.getPrefix(context.getSender());
        String suffix = prefixProvider.getSuffix(context.getSender());

        Component newFormat = context.getFormat();

        if (prefix != null) {
            newFormat = newFormat.replaceText(TextReplacementConfig.builder()
                    .match("{prefix}")
                    .replacement(prefix)
                    .build());
        }

        if (suffix != null) {
            newFormat = newFormat.replaceText(TextReplacementConfig.builder()
                    .match("{suffix}")
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

}

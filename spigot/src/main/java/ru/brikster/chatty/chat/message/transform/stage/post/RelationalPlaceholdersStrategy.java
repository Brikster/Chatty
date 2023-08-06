package ru.brikster.chatty.chat.message.transform.stage.post;

import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.TwoPlayersTransformContext;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.chat.message.transform.AbstractComponentTransformerStrategy;
import ru.brikster.chatty.config.type.SettingsConfig;
import ru.brikster.chatty.config.type.SettingsConfig.RelationalPlaceholdersOrder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class RelationalPlaceholdersStrategy extends AbstractComponentTransformerStrategy<String, TwoPlayersTransformContext> {

    @Inject
    public RelationalPlaceholdersStrategy(RelationalPlaceholdersComponentTransformer placeholdersComponentTransformer, SettingsConfig config) {
        super(placeholdersComponentTransformer, context ->
                    config.getRelationalPlaceholdersOrder() == RelationalPlaceholdersOrder.SENDER_AND_TARGET
                            ? TwoPlayersTransformContext.of(context.getSender(), context.getTarget())
                            : TwoPlayersTransformContext.of(context.getTarget(), context.getSender())
                );
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.POST;
    }

}

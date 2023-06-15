package ru.brikster.chatty.chat.component.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.prefix.PrefixProvider;

import javax.inject.Inject;

public final class PrefixComponentTransformer implements ComponentTransformer<SinglePlayerTransformContext> {

    @Inject
    private PrefixProvider prefixProvider;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        String prefix = prefixProvider.getPrefix(context.getPlayer());
        String suffix = prefixProvider.getSuffix(context.getPlayer());

        Component newComponent = formatComponent;

        if (prefix != null) {
            newComponent = newComponent.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{prefix}")
                    .replacement(prefix)
                    .build());
        }

        if (suffix != null) {
            newComponent = newComponent.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{suffix}")
                    .replacement(suffix)
                    .build());
        }

        return newComponent;
    }

}

package ru.brikster.chatty.chat.component.impl;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.ObjectUtil;

import javax.inject.Inject;
import java.util.regex.Pattern;

public final class PrefixComponentTransformer implements ComponentTransformer<SinglePlayerTransformContext> {

    private static final Pattern PREFIX_SUFFIX_PATTERN = Pattern.compile("\\{prefix}|\\{suffix}");

    @Inject
    private PrefixProvider prefixProvider;

    @Inject
    private ComponentStringConverter componentStringConverter;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        String prefix = prefixProvider.getPrefix(context.getPlayer());
        String suffix = prefixProvider.getSuffix(context.getPlayer());
        return AdventureUtil.replaceWithEndingSpace(formatComponent, PREFIX_SUFFIX_PATTERN, matchedString -> {
           if (matchedString.equals("{prefix}")) {
               return componentStringConverter.stringToComponent(ObjectUtil.requireNonNullElse(prefix, "") + " ");
           } else {
               return componentStringConverter.stringToComponent(ObjectUtil.requireNonNullElse(suffix, "") + " ");
           }
        });
    }

}

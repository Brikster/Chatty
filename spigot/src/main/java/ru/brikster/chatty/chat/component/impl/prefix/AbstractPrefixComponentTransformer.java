package ru.brikster.chatty.chat.component.impl.prefix;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.brikster.chatty.util.AdventureUtil;
import ru.brikster.chatty.util.ObjectUtil;

import java.util.regex.Pattern;

public abstract class AbstractPrefixComponentTransformer implements ComponentTransformer<SinglePlayerTransformContext> {

    private final PrefixProvider prefixProvider;
    private final ComponentStringConverter componentStringConverter;
    private final Pattern prefixOrSuffixPattern;
    private final String prefixPlaceholder;
    private final String suffixPlaceholder;

    public AbstractPrefixComponentTransformer(PrefixProvider prefixProvider,
                                              ComponentStringConverter componentStringConverter,
                                              Pattern prefixOrSuffixPattern,
                                              String prefixPlaceholder,
                                              String suffixPlaceholder) {
        this.prefixProvider = prefixProvider;
        this.componentStringConverter = componentStringConverter;
        this.prefixOrSuffixPattern = prefixOrSuffixPattern;
        this.prefixPlaceholder = prefixPlaceholder;
        this.suffixPlaceholder = suffixPlaceholder;
    }

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        String prefix = prefixProvider.getPrefix(context.getPlayer());
        String suffix = prefixProvider.getSuffix(context.getPlayer());
        return AdventureUtil.replaceWithEndingSpace(formatComponent, prefixOrSuffixPattern, matchedString -> {
            String result = replace(prefix, suffix, matchedString);
            return componentStringConverter.stringToComponent(result + " ");
        }, matchedString -> replace(prefix, suffix, matchedString));
    }

    private String replace(String prefix, String suffix, String matchedString) {
        if (matchedString.equals(prefixPlaceholder)) {
            return ObjectUtil.requireNonNullElse(prefix, "");
        } else if (matchedString.equals(suffixPlaceholder)) {
            return ObjectUtil.requireNonNullElse(suffix, "");
        } else {
            throw new IllegalStateException("Illegal string matched: " + matchedString);
        }
    }

}

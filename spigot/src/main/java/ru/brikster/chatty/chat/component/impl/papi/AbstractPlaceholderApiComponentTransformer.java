package ru.brikster.chatty.chat.component.impl.papi;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import java.util.function.Function;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class AbstractPlaceholderApiComponentTransformer implements PlaceholdersComponentTransformer {

    private final ComponentStringConverter componentStringConverter;
    private final Pattern placeholderPattern;
    private final Function<String, String> matchedStringTransformFunction;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        return AdventureUtil.replaceWithEndingSpace(formatComponent, placeholderPattern, matchedString -> {
            String result = replace(context, matchedString);
            return result == null ? null : componentStringConverter.stringToComponent(result + " ");
        }, matchedString -> replace(context, matchedString));
    }

    private String replace(SinglePlayerTransformContext context, String matchedString) {
        String matchedTransformed = matchedStringTransformFunction.apply(matchedString);
        String matchedWithPlaceholders = PlaceholderAPI.setPlaceholders(context.getPlayer(), matchedTransformed);
        if (matchedWithPlaceholders.equals(matchedString)) {
            return null;
        } else {
            return matchedWithPlaceholders;
        }
    }

}

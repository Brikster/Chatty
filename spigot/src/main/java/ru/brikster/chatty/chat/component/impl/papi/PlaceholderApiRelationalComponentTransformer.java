package ru.brikster.chatty.chat.component.impl.papi;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.TwoPlayersTransformContext;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor
public final class PlaceholderApiRelationalComponentTransformer extends RelationalPlaceholdersComponentTransformer {

    private static final Pattern RELATIONAL_PLACEHOLDER_PATTERN = Pattern.compile("%(rel_)([^%]+)%");

    private final ComponentStringConverter componentStringConverter;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull TwoPlayersTransformContext context) {
        return AdventureUtil.replaceWithEndingSpace(formatComponent, RELATIONAL_PLACEHOLDER_PATTERN, matchedString -> {
            String matchedWithPlaceholders = PlaceholderAPI.setRelationalPlaceholders(context.getOne(), context.getTwo(), matchedString);
            if (matchedWithPlaceholders.equals(matchedString)) {
                return null;
            } else {
                return componentStringConverter.stringToComponent(matchedWithPlaceholders + " ");
            }
        }, matchedString -> {
            String matchedWithPlaceholders = PlaceholderAPI.setRelationalPlaceholders(context.getOne(), context.getTwo(), matchedString);
            if (matchedWithPlaceholders.equals(matchedString)) {
                return null;
            } else {
                return matchedWithPlaceholders;
            }
        });
    }

}
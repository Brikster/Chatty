package ru.brikster.chatty.chat.component.impl.papi;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.chat.component.impl.PlaceholdersComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor
public final class PlaceholderApiComponentTransformer extends PlaceholdersComponentTransformer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

    private final ComponentStringConverter componentStringConverter;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        return AdventureUtil.replaceWithEndingSpace(formatComponent, PLACEHOLDER_PATTERN, matchedString -> {
            String matchedWithPlaceholders = PlaceholderAPI.setPlaceholders(context.getPlayer(), matchedString);
            if (matchedWithPlaceholders.equals(matchedString)) {
                return null;
            } else {
                return componentStringConverter.stringToComponent(matchedWithPlaceholders + " ");
            }
        });
    }

}

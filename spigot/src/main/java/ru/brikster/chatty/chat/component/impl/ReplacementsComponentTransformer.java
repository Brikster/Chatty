package ru.brikster.chatty.chat.component.impl;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.Constants;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.config.type.ReplacementsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import java.util.Set;

@RequiredArgsConstructor
public final class ReplacementsComponentTransformer implements PlaceholdersComponentTransformer {

    private final ReplacementsConfig replacementsConfig;
    private final ComponentStringConverter componentStringConverter;
    private final Set<String> cycledReplacements;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull SinglePlayerTransformContext context) {
        Component componentWithReplacements = formatComponent;

        // TODO optimize deep replacement

        int[] matches = new int[1];
        do {
            matches[0] = 0;
            componentWithReplacements = AdventureUtil.replaceWithEndingSpace(componentWithReplacements, Constants.REPLACEMENTS_PATTERN, matchedString -> {
                String result = replace(matchedString);
                Component component = componentStringConverter.stringToComponent(result + " ");
                if (result != null) {
                    matches[0]++;
                    return component;
                }
                return null;
            }, matchedString -> {
                String result = replace(matchedString);
                if (result != null) {
                    matches[0]++;
                    return result;
                }
                return null;
            });
        } while (matches[0] > 0);

        return componentWithReplacements;
    }

    private String replace(String matchedString) {
        String replacementKey = matchedString.substring(3, matchedString.length() - 1);
        if (cycledReplacements.contains(replacementKey)) return null;
        return replacementsConfig.getReplacements().get(replacementKey);
    }

}

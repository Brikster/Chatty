package ru.brikster.chatty.chat.component.impl.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.TwoPlayersTransformContext;
import ru.brikster.chatty.chat.component.impl.RelationalPlaceholdersComponentTransformer;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.regex.Pattern;

@Singleton
public final class PlaceholderApiRelationalComponentTransformer extends RelationalPlaceholdersComponentTransformer {

    private static final Pattern RELATIONAL_PLACEHOLDER_PATTERN = Pattern.compile("%(rel_)([^%]+)%");

    private final ComponentStringConverter componentStringConverter;
    private final SettingsConfig settingsConfig;
    private static final LegacyComponentSerializer PLACEHOLDER_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    @Inject
    public PlaceholderApiRelationalComponentTransformer(ComponentStringConverter componentStringConverter,
                                                        SettingsConfig settingsConfig) {
        this.componentStringConverter = componentStringConverter;
        this.settingsConfig = settingsConfig;
    }

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent, @NotNull TwoPlayersTransformContext context) {
        return AdventureUtil.replaceWithEndingSpace(formatComponent, RELATIONAL_PLACEHOLDER_PATTERN, matchedString -> {
            String result = replace(context, matchedString);
            if (result == null) {
                return null;
            }
            Component component = settingsConfig.isAllowPlaceholderMiniMessage()
                    ? componentStringConverter.stringToComponent(result)
                    : PLACEHOLDER_SERIALIZER.deserialize(result.replace('§', '&'));
            return component.append(Component.text(" "));
        }, matchedString -> {
            String result = replace(context, matchedString);
            if (result == null) {
                return null;
            }
            return settingsConfig.isAllowPlaceholderMiniMessage()
                    ? result
                    : result.replace('§', '&');
        });
    }

    private String replace(TwoPlayersTransformContext context, String matchedString) {
        String matchedWithPlaceholders = PlaceholderAPI.setRelationalPlaceholders(context.getOne(), context.getTwo(), matchedString);
        if (matchedWithPlaceholders.equals(matchedString)) {
            return null;
        } else {
            return matchedWithPlaceholders;
        }
    }

}

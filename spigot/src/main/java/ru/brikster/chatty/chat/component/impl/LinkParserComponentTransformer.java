package ru.brikster.chatty.chat.component.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.SinglePlayerTransformContext;
import ru.brikster.chatty.config.type.SettingsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Singleton
public final class LinkParserComponentTransformer implements ComponentTransformer<SinglePlayerTransformContext> {

    @Inject
    private SettingsConfig settingsConfig;

    @Inject
    private ComponentStringConverter componentStringConverter;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent,
                                        @NotNull SinglePlayerTransformContext context) {
        return AdventureUtil.replaceWithEndingSpace(formatComponent, settingsConfig.getLinksParsing().getPattern(), (matchedString) -> {
            try {
                URL url = new URI(matchedString).toURL();
                return Component.text(url + " ")
                        .clickEvent(ClickEvent.openUrl(url))
                        .hoverEvent(componentStringConverter.stringToComponent(settingsConfig.getLinksParsing().getHoverMessage()));
            } catch (MalformedURLException | URISyntaxException e) {
                return null;
            }
        }, __ -> null);
    }

}

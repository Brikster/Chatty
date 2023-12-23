package ru.brikster.chatty.chat.component.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.ComponentTransformer;
import ru.brikster.chatty.chat.component.context.EmptyTransformContext;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Singleton
public final class LinkParserComponentTransformer implements ComponentTransformer<EmptyTransformContext> {

    @Inject
    private SettingsConfig settingsConfig;

    @Inject
    private ComponentStringConverter componentStringConverter;

    @Override
    public @NotNull Component transform(@NotNull Component formatComponent,
                                        @NotNull EmptyTransformContext context) {
        return formatComponent.replaceText(TextReplacementConfig.builder()
                .match(settingsConfig.getLinksParsing().getPattern())
                .replacement(((matchResult, builder) -> {
                    try {
                        URL url = new URI(matchResult.group()).toURL();
                        return Component.text(url.toString())
                                .clickEvent(ClickEvent.openUrl(url))
                                .hoverEvent(componentStringConverter.stringToComponent(settingsConfig.getLinksParsing().getHoverMessage()));
                    } catch (MalformedURLException | URISyntaxException e) {
                        return null;
                    }
                }))
                .build());
    }

}

package ru.brikster.chatty.chat.message.transform.stage.middle;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.api.chat.message.strategy.MessageTransformStrategy;
import ru.brikster.chatty.api.chat.message.strategy.result.MessageTransformResult;
import ru.brikster.chatty.chat.message.transform.result.MessageTransformResultBuilder;
import ru.brikster.chatty.config.type.SettingsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public final class LinkParserTransformStrategy implements MessageTransformStrategy<Component> {

    @Inject
    private SettingsConfig settingsConfig;

    @Inject
    private ComponentStringConverter componentStringConverter;

    @Override
    public @NotNull MessageTransformResult<Component> handle(MessageContext<Component> context) {
        if (!context.getChat().isParseLinks()) {
            return MessageTransformResultBuilder.<Component>fromContext(context).build();
        }
        return MessageTransformResultBuilder.<Component>fromContext(context)
                .withMessage(AdventureUtil.replaceWithEndingSpace(context.getMessage(), settingsConfig.getLinksParsing().getPattern(), (matchedString) -> {
                    try {
                        URL url = new URL(matchedString);
                        return Component.text(url + " ")
                                .clickEvent(ClickEvent.openUrl(url))
                                .hoverEvent(componentStringConverter.stringToComponent(settingsConfig.getLinksParsing().getHoverMessage()));
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }))
                .build();
    }

    @Override
    public @NotNull Stage getStage() {
        return Stage.MIDDLE;
    }

}

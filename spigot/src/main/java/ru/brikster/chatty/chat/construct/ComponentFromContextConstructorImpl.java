package ru.brikster.chatty.chat.construct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import ru.brikster.chatty.api.chat.message.context.MessageContext;
import ru.brikster.chatty.chat.component.impl.ReplacementsStringTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.util.AdventureUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class ComponentFromContextConstructorImpl implements ComponentFromContextConstructor {

    @Inject
    private ComponentStringConverter componentStringConverter;

    @Inject
    private LegacyToMiniMessageConverter legacyToMiniMessageConverter;

    @Inject
    private ReplacementsStringTransformer replacementsStringTransformer;

    private static final Pattern PLAYER_OR_MESSAGE_PLACEHOLDER = Pattern.compile("\\{player}|\\{message}");

    private static final String PLAYER_FORMAT_PLACEHOLDER = "{player}";
    private static final String MESSAGE_FORMAT_PLACEHOLDER = "{message}";

    @Override
    public Component construct(MessageContext<Component> context) {
        String messageWithMmFormat = componentStringConverter.componentToString(context.getMessage());
        String convertedMessageFormat = replacementsStringTransformer.transform(context.getSender(),
                legacyToMiniMessageConverter.convert(context.getMessageFormat()));
        // Convert any § legacy codes injected by other plugins (e.g. InteractiveChat
        // hover commands) to MiniMessage tags, otherwise the strict MiniMessage
        // deserializer below would throw on detecting legacy formatting codes.
        String formattedMessage = legacyToMiniMessageConverter.convertSectionCodes(convertedMessageFormat
                .replace("{original-message}", messageWithMmFormat));
        Component formattedMessageComponent = MiniMessage.miniMessage().deserialize(formattedMessage);

        return AdventureUtil.replaceWithEndingSpace(context.getFormat(),
                PLAYER_OR_MESSAGE_PLACEHOLDER,
                matchedString -> {
                    if (matchedString.equals(PLAYER_FORMAT_PLACEHOLDER)) {
                        return Component.text(context.getSender().getDisplayName() + " ");
                    } else if (matchedString.equals(MESSAGE_FORMAT_PLACEHOLDER)) {
                        return formattedMessageComponent.append(Component.text(" "));
                    }
                    throw new IllegalStateException("Cannot replace player or message placeholder: " + matchedString);
                },
                matchedString -> {
                    if (matchedString.equals(PLAYER_FORMAT_PLACEHOLDER)) {
                        return context.getSender().getDisplayName() + " ";
                    } else if (matchedString.equals(MESSAGE_FORMAT_PLACEHOLDER)) {
                        return PlainTextComponentSerializer.plainText().serialize(formattedMessageComponent) + " ";
                    }
                    throw new IllegalStateException("Cannot replace player or message placeholder: " + matchedString);
                });
    }

}

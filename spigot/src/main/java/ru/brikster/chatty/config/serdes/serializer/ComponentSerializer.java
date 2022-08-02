package ru.brikster.chatty.config.serdes.serializer;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.brikster.chatty.convert.component.ComponentConverter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComponentSerializer extends BidirectionalTransformer<String, Component> {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ComponentConverter converter;

    @Override
    public GenericsPair<String, Component> getPair() {
        return this.genericsPair(String.class, Component.class);
    }

    @Override
    public Component leftToRight(@NonNull String message, @NonNull SerdesContext serdesContext) {
        return converter.convert(message);
    }

    @Override
    public String rightToLeft(@NonNull Component component, @NonNull SerdesContext serdesContext) {
        return MINI_MESSAGE.serialize(component);
    }

}

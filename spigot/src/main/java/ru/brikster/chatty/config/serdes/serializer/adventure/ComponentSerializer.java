package ru.brikster.chatty.config.serdes.serializer.adventure;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import java.util.WeakHashMap;

@RequiredArgsConstructor
public final class ComponentSerializer extends BidirectionalTransformer<String, Component> {

    private final ComponentStringConverter converter;

    private final WeakHashMap<Component, String> componentStringWeakHashMap = new WeakHashMap<>();

    @Override
    public GenericsPair<String, Component> getPair() {
        return this.genericsPair(String.class, Component.class);
    }

    @Override
    public Component leftToRight(@NonNull String value, @NonNull SerdesContext serdesContext) {
        Component component = converter.stringToComponent(value);
        componentStringWeakHashMap.put(component, value);
        return component;
    }

    @Override
    public String rightToLeft(@NonNull Component value, @NonNull SerdesContext serdesContext) {
        return componentStringWeakHashMap.getOrDefault(value, converter.componentToString(value));
    }

}

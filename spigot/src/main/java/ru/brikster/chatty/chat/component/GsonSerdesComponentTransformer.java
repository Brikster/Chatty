package ru.brikster.chatty.chat.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.chat.component.context.TransformContext;

public abstract class GsonSerdesComponentTransformer<TransformContextT extends TransformContext> implements ComponentTransformer<TransformContextT> {

    private static final GsonComponentSerializer COMPONENT_SERIALIZER = GsonComponentSerializer.gson();

    @Override
    public final @NotNull Component transform(@NotNull Component component, @NotNull TransformContextT transformContext) {
        String componentAsString = COMPONENT_SERIALIZER.serialize(component);
        componentAsString = transform(componentAsString, transformContext);
        return COMPONENT_SERIALIZER.deserialize(componentAsString);
    }

    public abstract @NotNull String transform(@NotNull String serializedString, @NotNull TransformContextT transformContext);

}

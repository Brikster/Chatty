package ru.brikster.chatty.config.serdes;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;
import ru.brikster.chatty.config.serdes.serializer.ChatCommandConfigDeclarationSerializer;
import ru.brikster.chatty.config.serdes.serializer.ChatConfigDeclarationSerializer;
import ru.brikster.chatty.config.serdes.serializer.adventure.ComponentSerializer;
import ru.brikster.chatty.config.serdes.serializer.adventure.SoundSerializer;
import ru.brikster.chatty.convert.component.ComponentConverter;

import lombok.NonNull;

public class SerdesChatty implements OkaeriSerdesPack {

    private final ComponentConverter converter;

    public SerdesChatty(ComponentConverter converter) {
        this.converter = converter;
    }

    @Override
    public void register(@NonNull SerdesRegistry registry) {
        registry.register(new ChatConfigDeclarationSerializer());
        registry.register(new ChatCommandConfigDeclarationSerializer());

        registry.register(new SoundSerializer());
        registry.register(new ComponentSerializer(converter));
    }

}

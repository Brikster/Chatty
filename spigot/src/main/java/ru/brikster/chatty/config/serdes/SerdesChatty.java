package ru.brikster.chatty.config.serdes;

import com.google.inject.Injector;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;
import ru.brikster.chatty.config.serdes.serializer.ChatCommandConfigDeclarationSerializer;
import ru.brikster.chatty.config.serdes.serializer.ChatConfigDeclarationSerializer;
import ru.brikster.chatty.config.serdes.serializer.ComponentSerializer;

import lombok.NonNull;

public class SerdesChatty implements OkaeriSerdesPack {

    private final Injector injector;

    public SerdesChatty(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void register(@NonNull SerdesRegistry registry) {
        registry.register(new ChatConfigDeclarationSerializer());
        registry.register(new ChatCommandConfigDeclarationSerializer());

        ComponentSerializer componentSerializer = new ComponentSerializer();
        injector.injectMembers(componentSerializer);

        registry.register(componentSerializer);
    }

}

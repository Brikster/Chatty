package ru.brikster.chatty.config.serdes.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import ru.brikster.chatty.config.object.ChatProperties;
import ru.brikster.chatty.config.object.ChatProperties.ChatCommandProperties;

import java.util.Optional;

public class ChatConfigDeclarationSerializer implements ObjectSerializer<ChatProperties> {

    @Override
    public boolean supports(@NonNull Class<? super ChatProperties> type) {
        return ChatProperties.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull ChatProperties chat, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("display-name", chat.getDisplayName());
        data.add("format", chat.getFormat());
        data.add("symbol", chat.getSymbol());
        data.add("range", chat.getRange());
        data.add("cooldown", chat.getCooldown());

        if (chat.getCommand() != null) {
            data.add("command", chat.getCommand(), ChatCommandProperties.class);
        }
    }

    @Override
    public ChatProperties deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String displayName = data.get("display-name", String.class);
        String format = data.get("format", String.class);
        String symbol = data.get("symbol", String.class);
        int range = Optional.ofNullable(data.get("range", Integer.class)).orElse(-2);
        int cooldown = Optional.ofNullable(data.get("cooldown", Integer.class)).orElse(0);
        ChatCommandProperties command = data.get("command", ChatCommandProperties.class);
        // Todo rewrite
        return new ChatProperties(displayName, format, symbol, range, cooldown, 0, false, false, command);
    }

}

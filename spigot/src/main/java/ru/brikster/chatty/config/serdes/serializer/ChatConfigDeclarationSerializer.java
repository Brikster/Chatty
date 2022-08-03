package ru.brikster.chatty.config.serdes.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import ru.brikster.chatty.config.object.ChatConfigDeclaration;
import ru.brikster.chatty.config.object.ChatConfigDeclaration.ChatCommandConfigDeclaration;

import lombok.NonNull;

import java.util.Optional;

public class ChatConfigDeclarationSerializer implements ObjectSerializer<ChatConfigDeclaration> {

    @Override
    public boolean supports(@NonNull Class<? super ChatConfigDeclaration> type) {
        return ChatConfigDeclaration.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull ChatConfigDeclaration chat, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("display-name", chat.getDisplayName());
        data.add("format", chat.getFormat());
        data.add("symbol", chat.getSymbol());
        data.add("range", chat.getRange());
        data.add("cooldown", chat.getCooldown());

        if (chat.getCommand() != null) {
            data.add("command", chat.getCommand(), ChatCommandConfigDeclaration.class);
        }
    }

    @Override
    public ChatConfigDeclaration deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String displayName = data.get("display-name", String.class);
        String format = data.get("format", String.class);
        String symbol = data.get("symbol", String.class);
        int range = Optional.ofNullable(data.get("range", Integer.class)).orElse(-2);
        int cooldown = Optional.ofNullable(data.get("cooldown", Integer.class)).orElse(0);
        ChatCommandConfigDeclaration command = data.get("command", ChatCommandConfigDeclaration.class);
        // Todo rewrite
        return new ChatConfigDeclaration(displayName, format, symbol, range, cooldown, 0, false, false, command);
    }

}

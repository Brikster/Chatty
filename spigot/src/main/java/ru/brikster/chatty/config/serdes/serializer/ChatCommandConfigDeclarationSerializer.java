package ru.brikster.chatty.config.serdes.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import ru.brikster.chatty.config.object.ChatConfigDeclaration.ChatCommandConfigDeclaration;

import lombok.NonNull;

import java.util.List;

public class ChatCommandConfigDeclarationSerializer implements ObjectSerializer<ChatCommandConfigDeclaration> {

    @Override
    public boolean supports(@NonNull Class<? super ChatCommandConfigDeclaration> type) {
        return ChatCommandConfigDeclaration.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull ChatCommandConfigDeclaration command, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("name", command.getName());
        data.add("aliases", command.getAliases());
        data.add("allow-switch-with-command", command.isAllowSwitchWithCommand());
        data.add("read-only-switched", command.isReadOnlySwitched());
    }

    @Override
    public ChatCommandConfigDeclaration deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String name = data.get("name", String.class);
        List<String> aliases = data.getAsList("aliases", String.class);
        boolean allowSwitchWithCommand = data.get("allow-switch-with-command", boolean.class);
        boolean readOnlySwitched = data.get("read-only-switched", boolean.class);
        return new ChatCommandConfigDeclaration(name, aliases, allowSwitchWithCommand, readOnlySwitched);
    }

}

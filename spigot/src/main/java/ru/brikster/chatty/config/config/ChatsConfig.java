package ru.brikster.chatty.config.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import ru.brikster.chatty.config.object.ChatConfigDeclaration;
import ru.brikster.chatty.config.object.ChatConfigDeclaration.ChatCommandConfigDeclaration;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ChatsConfig extends OkaeriConfig {

    private Map<String, ChatConfigDeclaration> chats = new TreeMap<String, ChatConfigDeclaration>() {{
        put("global", new ChatConfigDeclaration(
                "Global", "&7[&2Global&7] &r<prefix><player><suffix>&8: &f<message>",
                "!", -2, 15, new ChatCommandConfigDeclaration(
                "gchat", Collections.emptyList(), true, false)
        ));

        put("local", new ChatConfigDeclaration(
                "Local", "&7[&cLocal&7] &r<prefix><player><suffix>&8: &f<message>",
                "", 100, 0, null
        ));
    }};

}

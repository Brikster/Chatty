package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import ru.brikster.chatty.config.object.ChatProperties;
import ru.brikster.chatty.config.object.ChatProperties.ChatCommandProperties;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ChatsConfig extends OkaeriConfig {

    private Map<String, ChatProperties> chats = new TreeMap<String, ChatProperties>() {{
        put("global", new ChatProperties(
                "Global", "&7[&2Global&7] &r{prefix}{player}{suffix}&8: &f{message}",
                "!", -2, 15, 0, false, false,
                new ChatCommandProperties("gchat", Collections.emptyList(), true, false)
        ));

        put("local", new ChatProperties(
                "Local", "&7[&cLocal&7] &r{prefix}{player}{suffix}&8: &f{message}",
                "", 100, 0, 0, false, true, null
        ));
    }};

}

package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import org.bukkit.event.EventPriority;
import ru.brikster.chatty.BuildConstants;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Header("################################################################")
@Header("#")
@Header("#    Chatty (version " + BuildConstants.VERSION + ")")
@Header("#    Author: Brikster")
@Header("#")
@Header("#    Optional dependencies: PlaceholderAPI, Vault")
@Header("#")
@Header("################################################################")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class SettingsConfig extends OkaeriConfig {

    @Comment({
            "",
            "Supported languages: en-us, ru-ru, de-de, zh-cn",
            "You can create own language file and put it into \"lang/<language>.yml\""
    })
    private String language = "en-us";

    @Comment({"",
            "Chat listener priority",
            "May be useful if Chatty conflicts with another plugin",
            "See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html"})
    private EventPriority listenerPriority = EventPriority.LOW;

    @Comment({"",
            "Should Chatty keep modified recipients list, ",
            "got after previous event handlers? ",
            "For example: Essentials event handler called earlier ",
            "and removed some players due to ignore list"
    })
    private boolean respectForeignRecipients = true;

}

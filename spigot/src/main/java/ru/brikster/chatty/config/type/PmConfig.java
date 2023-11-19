package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import ru.brikster.chatty.BuildConstants;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Header("################################################################")
@Header("#")
@Header("#    Chatty (version " + BuildConstants.VERSION + ")")
@Header("#    Author: Brikster")
@Header("#")
@Header("################################################################")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PmConfig extends OkaeriConfig {

    @Comment({"", "Enable private messages?"})
    private boolean enable = true;

    @Comment({"",
            "Allow private messages from/to console?",
            "Not recommended (placeholders won't parse for console)"})
    private boolean allowConsole = false;

    @Comment({"",
            "Private messages format.",
            "Supports PlaceholderAPI with special placeholders format: ",
            "%from:<placeholder>% and %to:<placeholder>%.",
            "For example, %player_health% should be %from:player_health% to display health of sender player."})
    private String fromFormat = "{from-prefix}{from-name}{from-suffix} &8-> &r{to-prefix}{to-name}{to-suffix}&8: &f{message}";

    @Comment({"",
        "Same format, but displayed for receiver"})
    private String toFormat = "{from-prefix}{from-name}{from-suffix} &8-> &r{to-prefix}{to-name}{to-suffix}&8: &f{message}";

    @Comment({"", "Parse links in private messages?"})
    private boolean parseLinks = true;

}

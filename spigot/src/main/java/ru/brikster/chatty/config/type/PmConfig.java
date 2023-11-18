package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PmConfig extends OkaeriConfig {

    @Comment({"Allow private messages from/to Console?"})
    private boolean allowConsole = true;

    @Comment({"",
            "Private messages format.",
            "Supports PlaceholderAPI with special placeholders format: ",
            "%from:<placeholder>% and %to:<placeholder>%.",
            "For example, %player_health% should be %from:player_health% to display health of sender player."})
    private String format = "{from-prefix}{from-player}{from-suffix} &8-> &r{to-prefix}{to-player}{to-suffix}&8: &f{message}";

    @Comment({"", "Parse links in private messages?"})
    private boolean parseLinks = true;

}

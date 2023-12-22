package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
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

    @Comment
    @Comment("Disable this, if you don't want to specify sound for private messages")
    private boolean playSound = false;

    private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

    @Comment({"", "Parse links in private messages?"})
    private boolean parseLinks = true;

    @Comment({"",
            "Permission for spy: chatty.spy.pm"
    })
    private PmSpyConfig spy = new PmSpyConfig(false,
            "&8[&eSPY&8] &r{from-prefix}{from-name}{from-suffix} &8-> &r{to-prefix}{to-name}{to-suffix}&8: &f{message}");

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class PmSpyConfig extends OkaeriConfig {

        @Comment({"Enable spy for private messages?"})
        private boolean enable;

        @Comment({"Custom format for spy message"})
        private String format;

    }

}

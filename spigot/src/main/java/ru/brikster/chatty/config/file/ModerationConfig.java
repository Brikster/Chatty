package ru.brikster.chatty.config.file;

import com.google.common.collect.Sets;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.validator.annotation.Max;
import eu.okaeri.validator.annotation.Min;
import eu.okaeri.validator.annotation.Positive;
import lombok.Getter;
import ru.brikster.chatty.BuildConstants;

import java.util.Set;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Header("################################################################")
@Header("#")
@Header("#    Chatty (version " + BuildConstants.VERSION + ")")
@Header("#    Author: Brikster")
@Header("#")
@Header("################################################################")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ModerationConfig extends OkaeriConfig {

    @Comment({
            "",
            "Caps moderation method"
    })
    private CapsModerationConfig caps = new CapsModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class CapsModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment({
                "",
                "Minimal length for processing with this method"
        })
        @Positive
        private int length = 6;

        @Comment({
                "",
                "Minimal percent of uppercase characters",
                "to handle as CAPS"
        })
        @Min(0) @Max(100)
        private int percent = 80;


        @Comment({
                "",
                "Cancel chat event? If false, ",
                "message will be just lowercased"
        })
        private boolean block = true;

    }

    @Comment({
            "",
            "Ads moderation method"
    })
    private AdvertisementModerationConfig advertisement = new AdvertisementModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class AdvertisementModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment({
                "",
                "Regular expression for IP addresses matching"
        })
        private Pattern ipPattern = Pattern.compile("\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,)(-]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,7})?");

        @Comment({
                "",
                "Regular expression for websites links matching"
        })
        private Pattern linkPattern = Pattern.compile("\\bhttps?://(?:www\\.)?[^\\s/$.?#].\\S*\\b");

        @Comment({
                "",
                "Cancel chat event? If false, ",
                "ad will be replaced"
        })
        private boolean block = true;

        @Comment({
                "",
                "Replacement for ad words"
        })
        private String replacement = "<advertisement>";

        @Comment({
                "",
                "Whitelist of allowed IP addresses and links"
        })
        private Set<String> whitelist = Sets.newHashSet("google.com", "127.0.0.1", "192.168.0.1", "192.168.1.1");

    }

    @Comment({
            "",
            "Swear moderation method.",
            "Swears and whitelist located in \"plugins/Chatty/swears\" folder. ",
            "\"swears.txt\" should contain regular expressions from new lines, ",
            "\"whitelist.txt\" - whitelist words from new line (case insensitive)"
    })
    private SwearModerationConfig swear = new SwearModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class SwearModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment({
                "",
                "Cancel chat event? If false, ",
                "swear will be replaced"
        })
        private boolean block = true;

        @Comment({
                "",
                "Replacement for swear words"
        })
        private String replacement = "<swear>";

    }

}

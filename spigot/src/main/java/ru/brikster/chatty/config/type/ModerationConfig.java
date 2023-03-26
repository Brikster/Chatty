package ru.brikster.chatty.config.type;

import com.google.common.collect.Sets;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.validator.annotation.Max;
import eu.okaeri.validator.annotation.Min;
import eu.okaeri.validator.annotation.Positive;

import lombok.Getter;

import java.util.Set;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ModerationConfig extends OkaeriConfig {

    private CapsModerationConfig caps = new CapsModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class CapsModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Positive
        private int length = 6;

        @Min(0) @Max(100)
        private int percent = 80;

        private boolean block = true;

    }

    private AdvertisementModerationConfig advertisement = new AdvertisementModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class AdvertisementModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        private Pattern ipPattern = Pattern.compile("\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,)(-]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,7})?");
        private Pattern linkPattern = Pattern.compile("\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,)(-]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,7})?");

        private boolean block = true;

        private String replacement = "<advertisement>";

        private Set<String> whitelist = Sets.newHashSet("google.com", "127.0.0.1", "192.168.0.1", "192.168.1.1");

    }

    private SwearModerationConfig swear = new SwearModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class SwearModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        private boolean block = true;

        private String replacement = "<swear>";

    }

}

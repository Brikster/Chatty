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

    @Comment(value = {
            "",
            "Caps moderation method"}, language = "en-US")
    @Comment(value = {
            "",
            "Модерация капса"}, language = "ru-RU")
    private CapsModerationConfig caps = new CapsModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class CapsModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment(value = {
                "",
                "Minimal length for processing with this method"}, language = "en-US")
        @Comment(value = {
                "",
                "Минимальная длина сообщения для проверки"}, language = "ru-RU")
        @Positive
        private int length = 6;

        @Comment(value = {
                "",
                "Minimal percent of uppercase characters",
                "to handle as CAPS"}, language = "en-US")
        @Comment(value = {
                "",
                "Минимальный процент заглавных букв,",
                "чтобы считать сообщение капсом"}, language = "ru-RU")
        @Min(0) @Max(100)
        private int percent = 80;


        @Comment(value = {
                "",
                "Cancel chat event? If false, ",
                "message will be just lowercased"}, language = "en-US")
        @Comment(value = {
                "",
                "Отменять событие чата? Если false,",
                "сообщение просто станет строчным"}, language = "ru-RU")
        private boolean block = true;

    }

    @Comment(value = {
            "",
            "Ads moderation method"}, language = "en-US")
    @Comment(value = {
            "",
            "Модерация рекламы"}, language = "ru-RU")
    private AdvertisementModerationConfig advertisement = new AdvertisementModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class AdvertisementModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment(value = {
                "",
                "Regular expression for IP addresses matching"}, language = "en-US")
        @Comment(value = {
                "",
                "Регулярное выражение для поиска IP-адресов"}, language = "ru-RU")
        private Pattern ipPattern = Pattern.compile("\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,)(-]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,7})?");

        @Comment(value = {
                "",
                "Regular expression for websites links matching"}, language = "en-US")
        @Comment(value = {
                "",
                "Регулярное выражение для поиска ссылок на сайты"}, language = "ru-RU")
        private Pattern linkPattern = Pattern.compile("\\bhttps?://(?:www\\.)?[^\\s/$.?#].\\S*\\b");

        @Comment(value = {
                "",
                "Cancel chat event? If false, ",
                "ad will be replaced"}, language = "en-US")
        @Comment(value = {
                "",
                "Отменять событие чата? Если false,",
                "реклама будет заменена"}, language = "ru-RU")
        private boolean block = true;

        @Comment(value = {
                "",
                "Replacement for ad words"}, language = "en-US")
        @Comment(value = {
                "",
                "Замена для рекламных слов"}, language = "ru-RU")
        private String replacement = "<advertisement>";

        @Comment(value = {
                "",
                "Whitelist of allowed IP addresses and links"}, language = "en-US")
        @Comment(value = {
                "",
                "Белый список разрешённых IP-адресов и ссылок"}, language = "ru-RU")
        private Set<String> whitelist = Sets.newHashSet("google.com", "127.0.0.1", "192.168.0.1", "192.168.1.1");

    }

    @Comment(value = {
            "",
            "Swear moderation method.",
            "Swears and whitelist located in \"plugins/Chatty/swears\" folder. ",
            "\"swears.txt\" should contain regular expressions from new lines, ",
            "\"whitelist.txt\" - whitelist words from new line (case insensitive)"}, language = "en-US")
    @Comment(value = {
            "",
            "Модерация мата.",
            "Маты и белый список лежат в папке \"plugins/Chatty/swears\". ",
            "В \"swears.txt\" — регулярные выражения по одному на строку, ",
            "в \"whitelist.txt\" — разрешённые слова по одному на строку (без учёта регистра)"}, language = "ru-RU")
    private SwearModerationConfig swear = new SwearModerationConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class SwearModerationConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment(value = {
                "",
                "Cancel chat event? If false, ",
                "swear will be replaced"}, language = "en-US")
        @Comment(value = {
                "",
                "Отменять событие чата? Если false,",
                "мат будет заменён"}, language = "ru-RU")
        private boolean block = true;

        @Comment(value = {
                "",
                "Replacement for swear words"}, language = "en-US")
        @Comment(value = {
                "",
                "Замена для матерных слов"}, language = "ru-RU")
        private String replacement = "<swear>";

    }

}

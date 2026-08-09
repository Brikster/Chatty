package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;

import java.util.ArrayList;
import java.util.List;
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

    @Comment(value = {
            "",
            "Enable private messages?"}, language = "en-US")
    @Comment(value = {
            "",
            "Включить личные сообщения?"}, language = "ru-RU")
    private boolean enable = true;

    @Comment(value = {"",
            "Command and aliases used to send a private message.",
            "Remove an alias here if it clashes with another plugin",
            "(for example \"dm\", which DeluxeMenus also uses)."},
            language = "en-US")
    @Comment(value = {"",
            "Команда и её алиасы для отправки личного сообщения.",
            "Уберите алиас, если он конфликтует с другим плагином",
            "(например \"dm\", который использует и DeluxeMenus)."},
            language = "ru-RU")
    private String command = "msg";

    private List<String> aliases = new ArrayList<>(List.of("message", "m", "w", "pm", "tell", "whisper", "t"));

    @Comment(value = {
            "",
            "Command and aliases used to answer the last private message"}, language = "en-US")
    @Comment(value = {
            "",
            "Команда и псевдонимы для ответа на последнее личное сообщение"}, language = "ru-RU")
    private String replyCommand = "reply";

    private List<String> replyAliases = new ArrayList<>(List.of("r"));

    @Comment(value = {
            "",
            "Allow private messages from/to console?",
            "Not recommended (placeholders won't parse for console)"}, language = "en-US")
    @Comment(value = {
            "",
            "Разрешить личные сообщения от консоли и к ней?",
            "Не рекомендуется (для консоли плейсхолдеры не обрабатываются)"}, language = "ru-RU")
    private boolean allowConsole = false;

    @Comment(value = {
            "",
            "Private messages format.",
            "Supports PlaceholderAPI with special placeholders format: ",
            "%from:<placeholder>% and %to:<placeholder>%.",
            "For example, %player_health% should be %from:player_health% to display health of sender player."}, language = "en-US")
    @Comment(value = {
            "",
            "Формат личных сообщений.",
            "Поддерживает PlaceholderAPI с особым форматом плейсхолдеров: ",
            "%from:<плейсхолдер>% и %to:<плейсхолдер>%.",
            "Например, %player_health% нужно писать как %from:player_health%, чтобы показать здоровье отправителя."}, language = "ru-RU")
    private String fromFormat = "{from-prefix}{from-name}{from-suffix} &8-> &r{to-prefix}{to-name}{to-suffix}&8: &f{message}";

    @Comment(value = {
            "",
            "Same format, but displayed for receiver"}, language = "en-US")
    @Comment(value = {
            "",
            "Тот же формат, но для получателя"}, language = "ru-RU")
    private String toFormat = "{from-prefix}{from-name}{from-suffix} &8-> &r{to-prefix}{to-name}{to-suffix}&8: &f{message}";

    @Comment
    @Comment(value = "Disable this, if you don't want to specify sound for private messages", language = "en-US")
    @Comment(value = "Выключите, если не хотите задавать звук для личных сообщений", language = "ru-RU")
    private boolean playSound = false;

    private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

    @Comment(value = {
            "",
            "Parse links in private messages?"}, language = "en-US")
    @Comment(value = {
            "",
            "Обрабатывать ссылки в личных сообщениях?"}, language = "ru-RU")
    private boolean parseLinks = true;

    @Comment(value = {
            "",
            "Permission for spy: chatty.spy.pm"}, language = "en-US")
    @Comment(value = {
            "",
            "Право на слежку: chatty.spy.pm"}, language = "ru-RU")
    private PmSpyConfig spy = new PmSpyConfig(false,
            "&8[&eSPY&8] &r{from-prefix}{from-name}{from-suffix} &8-> &r{to-prefix}{to-name}{to-suffix}&8: &f{message}");

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class PmSpyConfig extends OkaeriConfig {

        @Comment(value = "Enable spy for private messages?", language = "en-US")
        @Comment(value = "Включить слежку за личными сообщениями?", language = "ru-RU")
        private boolean enable;

        @Comment(value = "Custom format for spy message", language = "en-US")
        @Comment(value = "Свой формат сообщения для слежки", language = "ru-RU")
        private String format;

    }

}

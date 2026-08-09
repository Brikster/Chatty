package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.event.EventPriority;
import ru.brikster.chatty.BuildConstants;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import java.util.Set;
import java.util.regex.Pattern;

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

    @Exclude
    public static ComponentStringConverter converter;

    @Exclude
    public static final Set<String> SUPPORTED_LANGUAGES =
            Set.of("en-US", "ru-RU", "de-DE", "es-ES", "zh-CN");

    public static String matchSupportedLanguage(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace('_', '-');
        for (String supported : SUPPORTED_LANGUAGES) {
            if (supported.equalsIgnoreCase(normalized)) {
                return supported;
            }
        }
        for (String supported : SUPPORTED_LANGUAGES) {
            if (supported.substring(0, supported.indexOf('-')).equalsIgnoreCase(normalized)) {
                return supported;
            }
        }
        return null;
    }

    @Comment(value = {
            "",
            "Supported languages: en-US, ru-RU, de-DE, es-ES, zh-CN.",
            "You can create own language file and put it into \"lang/<language>.yml\""
    }, language = "en-US")
    @Comment(value = {
            "",
            "Поддерживаемые языки: en-US, ru-RU, de-DE, es-ES, zh-CN.",
            "Вы можете создать собственный языковой файл и положить его в папку \"lang/<language>.yml\""
    }, language = "ru-RU")
    private String language = "en-US";

    @Comment(value = {"",
            "Chat listener priority",
            "May be useful if Chatty conflicts with another plugin",
            "See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html"},
            language = "en-US")
    @Comment(value = {"",
            "Приоритет слушателя чата",
            "Может быть полезно, если Chatty конфликтует с другими плагинами",
            "См. https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html"},
            language = "ru-RU")
    private EventPriority listenerPriority = EventPriority.LOW;

    @Comment(value = {"",
            "Should Chatty keep modified recipients list, ",
            "got after previous event handlers? ",
            "For example: Essentials event handler called earlier ",
            "and removed some players due to ignore list"
    }, language = "en-US")
    @Comment(value = {"",
            "Должен ли Chatty сохранять список получателей, ",
            "полученный после обработки события другими плагинами?",
            "Например: Essentials обрабатывает сообщение раньше",
            "и удаляет некоторых игроков из игнор-листа"
    }, language = "ru-RU")
    private boolean respectForeignRecipients = true;

    @Comment(value = {"",
            "Should Chatty ignore vanished recipients?",
            "This setting only affects \"no recipients\" message:",
            "if true, and everybody is vanished, Chatty will send it.",
            "Supports vanished players from Essentials and many others plugins, ",
            "that hides players with native Bukkit mechanism"
    }, language = "en-US")
    @Comment(value = {"",
            "Должен ли Chatty игнорировать скрытых получателей?",
            "Эта настройка влияет только на сообщение \"вас никто не услышал\":",
            "при true, когда все получатели скрыты, отправитель получит это сообщение.",
            "Поддерживает скрытых игроков из Essentials и многих других плагинов, ",
            "которые использует механизм скрытия Bukkit"
    }, language = "ru-RU")
    private boolean hideVanishedRecipients = true;

    @Comment(value = {
            "",
            "Order for handling relational placeholders",
            "from PlaceholderAPI (%rel_<placeholder>%).",
            "Values: SENDER_AND_TARGET, TARGET_AND_SENDER"}, language = "en-US")
    @Comment(value = {
            "",
            "Порядок обработки относительных плейсхолдеров",
            "из PlaceholderAPI (%rel_<плейсхолдер>%).",
            "Значения: SENDER_AND_TARGET, TARGET_AND_SENDER"}, language = "ru-RU")
    private RelationalPlaceholdersOrder relationalPlaceholdersOrder = RelationalPlaceholdersOrder.SENDER_AND_TARGET;

    @Comment(value = {
            "",
            "Settings for parsing links from player messages.",
            "See chats.yml for per-chat enabling"}, language = "en-US")
    @Comment(value = {
            "",
            "Настройки обработки ссылок из сообщений игроков.",
            "Включение для отдельных чатов — в chats.yml"}, language = "ru-RU")
    private LinksParsingConfig linksParsing = new LinksParsingConfig();

    @Comment(value = {
            "",
            "Send unsigned chat messages with sender's UUID.",
            "Helpful for enabling in-game ignore feature, but may cause newer client CRASHES"}, language = "en-US")
    @Comment(value = {
            "",
            "Отправлять неподписанные сообщения чата с UUID отправителя.",
            "Нужно для внутриигрового игнора, но может ВЫЗЫВАТЬ КРАШИ у новых клиентов"}, language = "ru-RU")
    private boolean sendIdentifiedMessages = false;

    @Comment(value = {"",
            "Rewrite configuration files after loading them?",
            "When true (default), Chatty re-saves every file on each load, which",
            "restores its own comments and drops anything it does not recognise.",
            "Set to false to keep your files exactly as you wrote them; new options",
            "from plugin updates then use their defaults without being added to the file."},
            language = "en-US")
    @Comment(value = {"",
            "Перезаписывать файлы конфигурации после загрузки?",
            "При true (по умолчанию) Chatty пересохраняет каждый файл при загрузке:",
            "возвращает свои комментарии и удаляет всё, что не распознал.",
            "Поставьте false, чтобы файлы оставались точно такими, как вы их написали;",
            "новые опции из обновлений тогда берут значения по умолчанию, не попадая в файл."},
            language = "ru-RU")
    private boolean rewriteConfigFiles = true;

    @Comment(value = {
            "",
            "Write every delivered chat message to plugins/Chatty/logs/"}, language = "en-US")
    @Comment(value = {
            "",
            "Записывать каждое доставленное сообщение в plugins/Chatty/logs/"}, language = "ru-RU")
    private ChatLogConfig chatLog = new ChatLogConfig();

    @Getter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class ChatLogConfig extends OkaeriConfig {

        private boolean enable = false;

    }

    @Comment(value = {
            "",
            "Enable debug messages"}, language = "en-US")
    @Comment(value = {
            "",
            "Включить отладочные сообщения"}, language = "ru-RU")
    private boolean debug = false;

    @Comment(value = {
            "",
            "Enable bStats metrics (anonymous)"}, language = "en-US")
    @Comment(value = {
            "",
            "Включить сбор статистики bStats (анонимно)"}, language = "ru-RU")
    private boolean sendMetrics = true;

    public enum RelationalPlaceholdersOrder {
        SENDER_AND_TARGET,
        TARGET_AND_SENDER
    }

    @Getter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class LinksParsingConfig extends OkaeriConfig {

        @Comment(value = "Pattern (regexp) for URLs parsing", language = "en-US")
        @Comment(value = "Шаблон (регулярное выражение) для поиска ссылок", language = "ru-RU")
        private Pattern pattern = Pattern.compile("(?i)\\bhttps?://\\S+\\b");

        @Comment(value = {
                "",
                "Hover message for parsed links"}, language = "en-US")
        @Comment(value = {
                "",
                "Всплывающая подсказка для найденных ссылок"}, language = "ru-RU")
        private String hoverMessage = "&bClick to follow the link";

        @Comment(value = {
                "",
                "Permission check (chatty.parselinks)"}, language = "en-US")
        @Comment(value = {
                "",
                "Проверка права (chatty.parselinks)"}, language = "ru-RU")
        private boolean permissionRequired = false;

    }

    @Comment(value = {
            "",
            "Settings for mentions"}, language = "en-US")
    @Comment(value = {
            "",
            "Настройки упоминаний"}, language = "ru-RU")
    private MentionsConfig mentions = new MentionsConfig();

    @Getter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class MentionsConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment(value = {
                "",
                "Pattern (regexp) for searching mentioned username"}, language = "en-US")
        @Comment(value = {
                "",
                "Шаблон (регулярное выражение) для поиска упомянутого ника"}, language = "ru-RU")
        private String pattern = "(?i)@{username}";

        @Comment(value = {
                "",
                "Format of mentioned username for others"}, language = "en-US")
        @Comment(value = {
                "",
                "Формат упомянутого ника для остальных"}, language = "ru-RU")
        private String othersFormat = "<hover:show_text:'&aClick to PM {username}'><click:suggest_command:'/msg {username} '>&a@{username}</click></hover>";

        @Comment(value = {
                "",
                "Format of mentioned username for it's owner"}, language = "en-US")
        @Comment(value = {
                "",
                "Формат упомянутого ника для его владельца"}, language = "ru-RU")
        private String targetFormat = "&e&l@{username}";

        @Comment
        @Comment(value = "Play sound on mention?", language = "en-US")
        @Comment(value = "Проигрывать звук при упоминании?", language = "ru-RU")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

    }

}

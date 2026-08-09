package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.validator.annotation.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import ru.brikster.chatty.BuildConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Header("################################################################")
@Header("#")
@Header("#    Chatty (version " + BuildConstants.VERSION + ")")
@Header("#    Author: Brikster")
@Header("#")
@Header("################################################################")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ChatsConfig extends OkaeriConfig {

    @Comment(value = {
            "",
            "List of chats.",
            "You can use declared or add you own chats"}, language = "en-US")
    @Comment(value = {
            "",
            "Список чатов.",
            "Можно использовать готовые или добавить свои"}, language = "ru-RU")
    private Map<String, ChatConfig> chats = new HashMap<>() {{
        put("local", new ChatConfig(
                "Local",
                "&7[<hover:show_text:'&bRange: 200 blocks'>&bLocal</hover>&7] &r{prefix}{player}{suffix}&8: &f{message}",
                "{original-message}",
                new HashMap<>(),
                new HashMap<>(),
                "",
                200,
                false,
                true,
                true,
                0,
                false,
                Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f),
                new ChatSpyConfig(true, "&6[Spy (local)] &r{prefix}{player}{suffix}&8: &f{message}"),
                "",
                new ArrayList<>(),
                true,
                false,
                ""));
        put("global", new ChatConfig(
                "Global",
                "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&6Global</click></hover>&7] &r{prefix}{player}{suffix}&8: &f{message}",
                "{original-message}",
                new HashMap<>() {{
                    put("red", new ChatStyleConfig(
                            "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&4Global</click></hover>&7] &r{prefix}{player}{suffix}&8: &c{message}",
                            "<gradient:#B14444:#972929>{original-message}</gradient>",
                            10
                    ));
                    put("green", new ChatStyleConfig(
                            "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&2Global</click></hover>&7] &r{prefix}{player}{suffix}&8: &a{message}",
                            "<gradient:#15B120:#19C224>{original-message}</gradient>",
                            20
                    ));
                }},
                new HashMap<>() {{
                    put("vip", new SenderFormatConfig(
                            10,
                            "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&6Global</click></hover>&7] &e[VIP] &r{prefix}{player}{suffix}&8: &f{message}",
                            "{original-message}",
                            new HashMap<>()));
                    put("admin", new SenderFormatConfig(
                            20,
                            "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&6Global</click></hover>&7] &c[Admin] &r{prefix}{player}{suffix}&8: &f{message}",
                            "{original-message}",
                            new HashMap<>() {{
                                put("red", new ChatStyleConfig(
                                        "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&4Global</click></hover>&7] &c[Admin] &r{prefix}{player}{suffix}&8: &c{message}",
                                        "<gradient:#B14444:#972929>{original-message}</gradient>",
                                        10));
                            }}));
                }},
                "!",
                -2,
                false,
                false,
                true,
                3,
                false,
                Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f),
                new ChatSpyConfig(false, ""),
                "",
                new ArrayList<>(),
                true,
                false,
                ""));
    }};

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatConfig extends OkaeriConfig {

        @Comment(value = {
                "Display name of chat.",
                "Used in commands, messages etc."}, language = "en-US")
        @Comment(value = {
                "Отображаемое название чата.",
                "Используется в командах, сообщениях и т. п."}, language = "ru-RU")
        private String displayName = "Unspecified";

        @Comment(value = {
                "",
                "Chat messages format.",
                "Supports: ",
                "* PlaceholderAPI (including relational placeholders)",
                "* MiniMessage interactive components (click handlers etc.)",
                "* Vault or LuckPerms prefixes/suffixes ({prefix} and {suffix})",
                "* Legacy color codes format (\"&c&lTHAT'S BOLD TEXT\")",
                "* Various hex formats (&#ffffff, {#ffffff}, &x&f&f&f&f&f&f etc.)",
                "",
                "Use https://webui.advntr.dev/ for convenient format creation.",
                "",
                "You can use replacements from \"replacements.yml\" here."}, language = "en-US")
        @Comment(value = {
                "",
                "Формат сообщений чата.",
                "Поддерживается: ",
                "* PlaceholderAPI (в том числе относительные плейсхолдеры)",
                "* интерактивные компоненты MiniMessage (клики и прочее)",
                "* префиксы и суффиксы из Vault или LuckPerms ({prefix} и {suffix})",
                "* старый формат цветовых кодов (\"&c&lЖИРНЫЙ ТЕКСТ\")",
                "* разные форматы hex (&#ffffff, {#ffffff}, &x&f&f&f&f&f&f и т. д.)",
                "",
                "Для удобного создания формата: https://webui.advntr.dev/",
                "",
                "Здесь можно использовать замены из \"replacements.yml\"."}, language = "ru-RU")
        private String format = "<{player}>: {message}";

        @Comment(value = {
                "",
                "Player message format (\"{message}\" part in \"format\" property).",
                "You can use gradient here to make player messages colorful.",
                "This part renders as if player message were explicitly written in MiniMessage component"}, language = "en-US")
        @Comment(value = {
                "",
                "Формат сообщения игрока (часть \"{message}\" в свойстве \"format\").",
                "Здесь можно задать градиент, чтобы сообщения были цветными.",
                "Эта часть обрабатывается так, будто игрок написал её компонентом MiniMessage"}, language = "ru-RU")
        private String messageFormat = "{original-message}";

        @Comment(value = {
                "",
                "Custom format styles. Players that have permission",
                "for a style will see all the messages from the chat",
                "with corresponding format.",
                "Permission: chatty.style.<style-name>, for example: chatty.style.red"}, language = "en-US")
        @Comment(value = {
                "",
                "Пользовательские стили формата. Игроки с правом на стиль",
                "будут видеть все сообщения чата в соответствующем формате.",
                "Право: chatty.style.<название-стиля>, например: chatty.style.red"}, language = "ru-RU")
        private Map<String, ChatStyleConfig> styles = new HashMap<>();

        @Comment(value = {"",
                "Per-rank appearance of THIS player's messages for everyone else.",
                "Chosen by the sender's permission, highest priority wins:",
                "  chatty.chat.<chat-id>.sender-format.<id>  (this chat only)",
                "  chatty.sender-format.<id>                 (every chat)",
                "Do not confuse with \"styles\" above: styles change how a chat",
                "looks to the player reading it, these change how a player looks",
                "to everybody. Each entry may carry its own styles."},
                language = "en-US")
        @Comment(value = {"",
                "Вид сообщений ЭТОГО игрока для всех остальных, по рангам.",
                "Выбирается по правам отправителя, побеждает высший приоритет:",
                "  chatty.chat.<chat-id>.sender-format.<id>  (только этот чат)",
                "  chatty.sender-format.<id>                 (все чаты)",
                "Не путайте со \"styles\" выше: styles меняют то, как чат видит",
                "читающий, а это — как выглядит сам игрок для остальных.",
                "У каждой записи могут быть свои styles."},
                language = "ru-RU")
        private Map<String, SenderFormatConfig> senderFormats = new HashMap<>();

        @Comment(value = {
                "",
                "Symbol (or prefix) that should be placed before message",
                "to send message into this that.",
                "Example for symbol: \"!\":",
                "!Hello world -> send message \"Hello world\" to this chat",
                "",
                "Empty symbol ('') is allowed also"}, language = "en-US")
        @Comment(value = {
                "",
                "Символ (или префикс), который ставится перед сообщением,",
                "чтобы отправить его в этот чат.",
                "Пример для символа \"!\":",
                "!Привет мир -> сообщение \"Привет мир\" уйдёт в этот чат",
                "",
                "Пустой символ ('') тоже допустим"}, language = "ru-RU")
        private String symbol = "";

        @Comment(value = {
                "",
                "Range in blocks for chat message recipients.",
                "Possible values: ",
                " -3 -> message will be sent to every server sharing this chat",
                "       (cross-server chat, requires proxy.yml to be configured)",
                " -2 -> message will be sent to all online players",
                " -1 -> message will be sent to all players of the sender's world",
                " >= 0 -> message will be sent to all players in this blocks range"}, language = "en-US")
        @Comment(value = {
                "",
                "Радиус в блоках, в котором получают сообщения чата.",
                "Возможные значения: ",
                " -3 -> сообщение уйдёт на все серверы, где есть этот чат",
                "       (межсерверный чат, требует настройки proxy.yml)",
                " -2 -> сообщение получат все игроки онлайн",
                " -1 -> сообщение получат все игроки в мире отправителя",
                " >= 0 -> сообщение получат все игроки в этом радиусе"}, language = "ru-RU")
        @Min(-3)
        private int range = -2;

        @Comment(value = {
                "",
                "If true, you must add permissions for using chat: ",
                " - chatty.chat.<chat-name> -> full chat access",
                " - chatty.chat.<chat-name>.read -> read access only",
                " - chatty.chat.<chat-name>.write -> write access only",
                "",
                "Example: chatty.chat.global -> full access for \"global\" chat"}, language = "en-US")
        @Comment(value = {
                "",
                "Если true, для доступа к чату нужно выдать права: ",
                " - chatty.chat.<название-чата> -> полный доступ к чату",
                " - chatty.chat.<название-чата>.read -> только чтение",
                " - chatty.chat.<название-чата>.write -> только запись",
                "",
                "Пример: chatty.chat.global -> полный доступ к чату \"global\""}, language = "ru-RU")
        private boolean permissionRequired = false;

        @Comment(value = {
                "",
                "If true, player will receive a special message, ",
                "when his message has no recipients.",
                "Message can be configured in locale files"}, language = "en-US")
        @Comment(value = {
                "",
                "Если true, игрок получит отдельное сообщение,",
                "когда его никто не услышал.",
                "Текст настраивается в языковых файлах"}, language = "ru-RU")
        private boolean notifyNobodyHeard = true;

        @Comment(value = {
                "",
                "If true, URLs from player messages will be processed",
                "and made clickable.",
                "Check settings.yml for more parameters"}, language = "en-US")
        @Comment(value = {
                "",
                "Если true, ссылки из сообщений игроков будут обработаны",
                "и станут кликабельными.",
                "Дополнительные параметры — в settings.yml"}, language = "ru-RU")
        private boolean parseLinks = true;

        @Comment(value = {
                "",
                "Cooldown in seconds for sending messages in chat.",
                "Bypass permission: chatty.bypass.cooldown.<chat>"}, language = "en-US")
        @Comment(value = {
                "",
                "Задержка в секундах между сообщениями в чат.",
                "Право на обход: chatty.bypass.cooldown.<чат>"}, language = "ru-RU")
        @Min(0)
        private int cooldown = 0;

        @Comment
        @Comment(value = "Disable this, if you don't want to specify sound for this chat", language = "en-US")
        @Comment(value = "Выключите, если не хотите задавать звук для этого чата", language = "ru-RU")
        private boolean playSound = false;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment(value = {
                "",
                "Permission for spy: chatty.spy.<chat>"}, language = "en-US")
        @Comment(value = {
                "",
                "Право на слежку: chatty.spy.<чат>"}, language = "ru-RU")
        private ChatSpyConfig spy = new ChatSpyConfig(false, "");

        @Comment(value = {"",
                "Command that writes into this chat: /<command> <message>.",
                "Leave empty to add no command. Running it without a message",
                "switches your chat to this one, so later messages need no symbol.",
                "Permission: chatty.chat.<chat-id>.write"},
                language = "en-US")
        @Comment(value = {"",
                "Команда для отправки в этот чат: /<команда> <сообщение>.",
                "Оставьте пустым, чтобы команды не было. Запуск без сообщения",
                "переключает ваш чат на этот, и дальше символ не нужен.",
                "Право: chatty.chat.<chat-id>.write"},
                language = "ru-RU")
        private String command = "";

        @Comment(value = {
                "",
                "Aliases for the chat command"}, language = "en-US")
        @Comment(value = {
                "",
                "Псевдонимы команды чата"}, language = "ru-RU")
        private List<String> aliases = new ArrayList<>();

        @Comment(value = {
                "",
                "Can the command be used without a message to switch chat?"}, language = "en-US")
        @Comment(value = {
                "",
                "Можно ли вызывать команду без сообщения, чтобы переключить чат?"}, language = "ru-RU")
        private boolean canSwitchWithCommand = true;

        @Comment(value = {
                "",
                "Deliver this chat only to players who switched into it?",
                "Useful for an opt-in chat nobody sees until they join it."}, language = "en-US")
        @Comment(value = {
                "",
                "Доставлять этот чат только тем, кто в него переключился?",
                "Удобно для чата, который не видно, пока в него не зайдёшь."}, language = "ru-RU")
        private boolean readOnlySwitched = false;

        @Comment(value = {"",
                "Deliver only to players whose placeholder value equals the sender's.",
                "Example: '%clan_name%' makes this a clan chat - a message reaches",
                "only players in the sender's clan. Empty disables the check.",
                "Needs PlaceholderAPI."},
                language = "en-US")
        @Comment(value = {"",
                "Отправлять только игрокам, у которых значение плейсхолдера",
                "совпадает со значением отправителя. Например, '%clan_name%'",
                "делает чат клановым: сообщение дойдёт только до соклановцев.",
                "Пусто — проверка выключена. Требует PlaceholderAPI, причём",
                "плейсхолдер должен им реально раскрываться: для %player_name%",
                "нужно расширение Player, иначе он останется текстом и",
                "совпадёт у всех."},
                language = "ru-RU")
        private String matchPlaceholder = "";

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatStyleConfig extends OkaeriConfig {

        @Comment(value = "Custom format for the style", language = "en-US")
        @Comment(value = "Свой формат для стиля", language = "ru-RU")
        private String format = "<{player}>: {message}";

        @Comment(value = {
                "",
                "Custom message format for the style"}, language = "en-US")
        @Comment(value = {
                "",
                "Свой формат сообщения для стиля"}, language = "ru-RU")
        private String messageFormat = "{original-message}";

        @Comment(value = {
                "",
                "If player has several permissions, chat with higher priority will be selected"}, language = "en-US")
        @Comment(value = {
                "",
                "Если у игрока несколько прав, выберется стиль с большим приоритетом"}, language = "ru-RU")
        private int priority = 0;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class SenderFormatConfig extends OkaeriConfig {

        @Comment(value = "Higher priority wins when the sender qualifies for several", language = "en-US")
        @Comment(value = "Побеждает больший приоритет, если отправителю подходит несколько", language = "ru-RU")
        private int priority = 0;

        private String format = "&7[&6VIP&7] &r{prefix}{player}{suffix}&8: &f{message}";

        private String messageFormat = "{original-message}";

        @Comment(value = {
                "",
                "How this format looks to a player holding chatty.style.<id>"}, language = "en-US")
        @Comment(value = {
                "",
                "Как этот формат выглядит для игрока с правом chatty.style.<id>"}, language = "ru-RU")
        private Map<String, ChatStyleConfig> styles = new HashMap<>();

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatSpyConfig extends OkaeriConfig {

        @Comment(value = "Enable spy for the chat?", language = "en-US")
        @Comment(value = "Включить слежку за чатом?", language = "ru-RU")
        private boolean enable;

        @Comment(value = "Custom format for spy message", language = "en-US")
        @Comment(value = "Свой формат сообщения для слежки", language = "ru-RU")
        private String format = "";

    }

}

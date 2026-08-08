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

    @Comment({"",
            "List of chats.",
            "You can use declared or add you own chats"})
    private Map<String, ChatConfig> chats = new HashMap<>() {{
        put("local", new ChatConfig(
                "Local",
                "&7[<hover:show_text:'&bRange: 200 blocks'>&bLocal</hover>&7] &r{prefix}{player}{suffix}&8: &f{message}",
                "{original-message}",
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
                false));
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
                false));
    }};

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatConfig extends OkaeriConfig {

        @Comment({
                "Display name of chat.",
                "Used in commands, messages etc."})
        private String displayName = "Unspecified";

        @Comment({"",
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
                "You can use replacements from \"replacements.yml\" here."
        })
        private String format = "<{player}>: {message}";

        @Comment({"",
            "Player message format (\"{message}\" part in \"format\" property).",
            "You can use gradient here to make player messages colorful.",
            "This part renders as if player message were explicitly written in MiniMessage component"})
        private String messageFormat = "{original-message}";

        @Comment({"",
                "Custom format styles. Players that have permission",
                "for a style will see all the messages from the chat",
                "with corresponding format.",
                "Permission: chatty.style.<style-name>, for example: chatty.style.red"
        })
        private Map<String, ChatStyleConfig> styles = new HashMap<>();

        @Comment({"",
                "Symbol (or prefix) that should be placed before message",
                "to send message into this that.",
                "Example for symbol: \"!\":",
                "!Hello world -> send message \"Hello world\" to this chat",
                "",
                "Empty symbol ('') is allowed also"})
        private String symbol = "";

        @Comment({"",
                "Range in blocks for chat message recipients.",
                "Possible values: ",
                " -3 -> message will be sent to every server sharing this chat",
                "       (cross-server chat, requires proxy.yml to be configured)",
                " -2 -> message will be sent to all online players",
                " -1 -> message will be sent to all players of the sender's world",
                " >= 0 -> message will be sent to all players in this blocks range"})
        @Min(-3)
        private int range = -2;

        @Comment({"",
                "If true, you must add permissions for using chat: ",
                " - chatty.chat.<chat-name> -> full chat access",
                " - chatty.chat.<chat-name>.read -> read access only",
                " - chatty.chat.<chat-name>.write -> write access only",
                "",
                "Example: chatty.chat.global -> full access for \"global\" chat"})
        private boolean permissionRequired = false;

        @Comment({
                "",
                "If true, player will receive a special message, ",
                "when his message has no recipients.",
                "Message can be configured in locale files"
        })
        private boolean notifyNobodyHeard = true;

        @Comment({
                "",
                "If true, URLs from player messages will be processed",
                "and made clickable.",
                "Check settings.yml for more parameters"
        })
        private boolean parseLinks = true;

        @Comment({
              "",
              "Cooldown in seconds for sending messages in chat.",
              "Bypass permission: chatty.bypass.cooldown.<chat>"
        })
        @Min(0)
        private int cooldown = 0;

        @Comment
        @Comment("Disable this, if you don't want to specify sound for this chat")
        private boolean playSound = false;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment({"",
                "Permission for spy: chatty.spy.<chat>"
        })
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

        @Comment({"", "Aliases for the chat command"})
        private List<String> aliases = new ArrayList<>();

        @Comment({"",
                "Can the command be used without a message to switch chat?"})
        private boolean canSwitchWithCommand = true;

        @Comment({"",
                "Deliver this chat only to players who switched into it?",
                "Useful for an opt-in chat nobody sees until they join it."})
        private boolean readOnlySwitched = false;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatStyleConfig extends OkaeriConfig {

        @Comment({"Custom format for the style"})
        private String format = "<{player}>: {message}";

        @Comment({"",
                "Custom message format for the style"})
        private String messageFormat = "{original-message}";

        @Comment({"",
                "If player has several permissions, chat with higher priority will be selected"})
        private int priority = 0;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatSpyConfig extends OkaeriConfig {

        @Comment({"Enable spy for the chat?"})
        private boolean enable;

        @Comment({"Custom format for spy message"})
        private String format = "";

    }

}

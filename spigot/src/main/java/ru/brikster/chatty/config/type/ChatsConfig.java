package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ChatsConfig extends OkaeriConfig {

//    private Map<String, ChatProperties> chats = new TreeMap<String, ChatProperties>() {{
//        put("global", new ChatProperties(
//                "Global", "&7[&2Global&7] &r{prefix}{player}{suffix}&8: &f{message}",
//                "!", -2, 15, 0, false, false,
//                new ChatCommandProperties("gchat", Collections.emptyList(), true, false)
//        ));
//
//        put("local", new ChatProperties(
//                "Local", "&7[&cLocal&7] &r{prefix}{player}{suffix}&8: &f{message}",
//                "", 100, 0, 0, false, true, null
//        ));
//    }};

    @Comment({"List of chats.",
            "You can use declared or add you own chats"})
    private Map<String, ChatConfig> chats = new HashMap<String, ChatConfig>() {{
        put("local", new ChatConfig(
                "Local",
                "&7[<hover:show_text:'&bRange: 200 blocks'>&bLocal</hover>&7] &r{prefix}{player}{suffix}&8: &f{message}",
                new HashMap<>(),
                "",
                200,
                false,
                true,
                true));
        put("global", new ChatConfig(
                "Global",
                "&7[<hover:show_text:'&aUse &2&l! &afor global chat'><click:suggest_command:!>&6Global</click></hover>&7] &r{prefix}{player}{suffix}&8: &f{message}",
                new HashMap<String, ChatStyleConfig>() {{
                    put("red", new ChatStyleConfig(
                            "&7[&4Global&7] &r{prefix}{player}{suffix}&8: &c{message}",
                            10
                    ));
                    put("green", new ChatStyleConfig(
                            "&7[&2Global&7] &r{prefix}{player}{suffix}&8: &a{message}",
                            10
                    ));
                }},
                "!",
                -2,
                false,
                false,
                true));
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
                "* Legacy colorcodes format (\"&c&lTHAT'S BOLD TEXT\")",
                "",
                "Use https://webui.advntr.dev/ for convenient format creation"
        })
        private String format = "<{player}>: {message}";

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
                " -2 -> message will be sent to all online players",
                " -1 -> message will be sent to all players of the sender's world",
                " >= 0 -> message will be sent to all players in this blocks range"})
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
                "and converted to clickable part.",
                "Check settings.yml for more parameters"
        })
        private boolean parseLinks = true;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static final class ChatStyleConfig extends OkaeriConfig {

        @Comment({"Custom format for the style"})
        private String format = "<{player}>: {message}";

        @Comment({"",
                "If player has several permissions, chat with higher priority will be selected"})
        private int priority = 0;

    }

}

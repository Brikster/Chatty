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

//    @Comment({
//            "",
//            "Supported languages: en-us, ru-ru, de-de, zh-cn",
//            "You can create own language file and put it into \"lang/<language>.yml\""
//    })
//    private String language = "en-us";

    @Comment({"",
            "Chat listener priority",
            "May be useful if Chatty conflicts with another plugin",
            "See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html"})
    private EventPriority listenerPriority = EventPriority.LOW;

    @Comment({"",
            "Should Chatty keep modified recipients list, ",
            "got after previous event handlers? ",
            "For example: Essentials event handler called earlier ",
            "and removed some players due to ignore list"
    })
    private boolean respectForeignRecipients = true;

    @Comment({"",
        "Should Chatty ignore vanished recipients?",
        "This setting only affects \"no recipients\" message:",
        "if true, and everybody is vanished, Chatty will send it.",
        "Supports vanished players from Essentials and many others plugins, ",
        "that hides players with native Bukkit mechanism"
    })
    private boolean hideVanishedRecipients = true;

    @Comment({"",
            "Order for handling relational placeholders",
            "from PlaceholderAPI (%rel_<placeholder>%).",
            "Values: SENDER_AND_TARGET, TARGET_AND_SENDER"
    })
    private RelationalPlaceholdersOrder relationalPlaceholdersOrder = RelationalPlaceholdersOrder.SENDER_AND_TARGET;

    @Comment({"", "Settings for parsing links from player messages.", "See chats.yml for per-chat enabling"})
    private LinksParsingConfig linksParsing = new LinksParsingConfig();

    @Comment({"",
            "Send unsigned chat messages with sender's UUID.",
            "Helpful for enabling in-game ignore feature, but may cause newer client CRASHES"})
    private boolean sendIdentifiedMessages = false;

    public enum RelationalPlaceholdersOrder {
        SENDER_AND_TARGET,
        TARGET_AND_SENDER
    }

    @Getter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class LinksParsingConfig extends OkaeriConfig {

        @Comment(
                {"Pattern (regexp) for URLs parsing"}
        )
        private Pattern pattern = Pattern.compile("(?i)\\bhttps?://\\S+\\b");

        @Comment({"", "Hover message for parsed links"})
        private String hoverMessage = "&bClick to follow the link";

        @Comment({"", "Permission check (chatty.parselinks)"})
        private boolean permissionRequired = false;

    }

    @Comment({"", "Settings for mentions"})
    private MentionsConfig mentions = new MentionsConfig();

    @Getter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class MentionsConfig extends OkaeriConfig {

        private boolean enable = true;

        @Comment({"",
                "Pattern (regexp) for searching mentioned username"})
        private String pattern = "(?i)@{username}";

        @Comment({"",
                "Format of mentioned username for others"})
        private String othersFormat = "<hover:show_text:'&aClick to PM {username}'><click:suggest_command:'/msg {username} '>&a@{username}</click></hover>";

        @Comment({"",
                "Format of mentioned username for it's owner"})
        private String targetFormat = "&e&l@{username}";

        @Comment
        @Comment("Play sound on mention?")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

    }

}

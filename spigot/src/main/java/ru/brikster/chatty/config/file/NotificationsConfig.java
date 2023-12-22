package ru.brikster.chatty.config.file;

import com.google.common.collect.Lists;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.validator.annotation.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import ru.brikster.chatty.BuildConstants;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import java.util.LinkedHashMap;
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
public class NotificationsConfig extends OkaeriConfig {

    @Exclude
    public static ComponentStringConverter converter;

    @Comment
    private ChatNotificationsConfig chat = new ChatNotificationsConfig();

    @Comment
    private ActionbarNotificationsConfig actionbar = new ActionbarNotificationsConfig();

    @Comment
    private TitleNotificationsConfig title = new TitleNotificationsConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class ChatNotificationsConfig extends OkaeriConfig {

        private boolean enable = true;

        private Map<String, ChatNotificationChannelConfig> lists = new LinkedHashMap<>() {{
            put("default", new ChatNotificationChannelConfig());
            put("donate", new ChatNotificationChannelConfig(
                    30,
                    Lists.newArrayList(
                            converter.stringToComponent("<rainbow>You can check for updates at " +
                                    "<click:open_url:'https://www.spigotmc.org/resources/chatty-lightweight-universal-bukkit-chat-system-solution-1-7-10-1-18.59411/'>" +
                                    "<yellow>spigotmc.org</yellow>" +
                                    "</click></rainbow>")),
                    false,
                    false
            ));
        }};

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class ChatNotificationChannelConfig extends OkaeriConfig {

            @Positive
            @Comment("Time in seconds for periodically broadcasting")
            private int period = 60;

            @Comment
            @Comment({
                    "Messages of chat notifications support MiniMessage format BOTH",
                    "legacy color codes with &, various hex-codes formats.",
                    "You can use convenient WebUI: https://webui.advntr.dev/"
            })
            private List<Component> messages = Lists.newArrayList(converter.stringToComponent(
                            "&8===================================\n" +
                                    "<gradient:#f3801f:#eb9115>Chatty</gradient> &7- &fawesome chat management system.\n" +
                                    "It supports <yellow>MiniMessage</yellow> format.\n" +
                                    "But you can use also legacy color codes with &b& &fsymbol.\n" +
                                    "Convenient website for preparing <yellow>MiniMessage</yellow>: " +
                                    "<click:open_url:'https://webui.advntr.dev/'><hover:show_text:'<green>MiniMessage Viewer'>&2click here</hover></click>.\n" +
                                    "&8==================================="),
                    converter.stringToComponent(
                            "&8===================================\n" +
                                    "&eSecond message from default channel\n" +
                                    "&8===================================")
            );

            @Comment
            @Comment("Enable this, if you want to restrict channel by permission")
            private boolean permissionRequired = false;

            @Comment
            @Comment("Enable this, if you want messages to be sent randomly")
            private boolean randomOrder = false;

        }

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class ActionbarNotificationsConfig extends OkaeriConfig {

        private boolean enable = true;

        private Map<String, ActionbarNotificationChannelConfig> lists = new LinkedHashMap<>() {{
            put("default", new ActionbarNotificationChannelConfig());
        }};

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class ActionbarNotificationChannelConfig extends OkaeriConfig {

            @Positive
            @Comment("Time in seconds for periodically broadcasting")
            private int period = 60;

            @Positive
            @Comment
            @Comment("Time in seconds for message stay")
            @Comment("Should be equal or lower than period")
            private int stay = 60;

            @Comment
            @Comment({
                    "Messages of chat notifications support MiniMessage format BOTH",
                    "legacy color codes with &, various hex-codes formats.",
                    "You can use convenient WebUI: https://webui.advntr.dev/"
            })
            private List<Component> messages = Lists.newArrayList(
                    converter.stringToComponent("&aFirst message from actionbar"),
                    converter.stringToComponent("&2Second message from actionbar")
            );

            @Comment
            @Comment("Enable this, if you want to restrict channel by permission")
            private boolean permissionRequired = false;

            @Comment
            @Comment("Enable this, if you want messages to be sent randomly")
            private boolean randomOrder = false;

        }

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class TitleNotificationsConfig extends OkaeriConfig {

        private boolean enable = true;

        private Map<String, TitleNotificationChannelConfig> lists = new LinkedHashMap<>() {{
            put("default", new TitleNotificationChannelConfig());
        }};

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class TitleNotificationChannelConfig extends OkaeriConfig {

            @Positive
            @Comment("Time in seconds for periodically broadcasting")
            private int period = 60;

            @Comment
            @Comment({
                    "Messages of title notifications support MiniMessage format BOTH",
                    "legacy color codes with &, various hex-codes formats.",
                    "You can use convenient WebUI: https://webui.advntr.dev/"
            })
            private List<TitleNotificationMessageConfig> messages = Lists.newArrayList(new TitleNotificationMessageConfig());

            @Comment
            @Comment("Enable this, if you want to restrict channel by permission")
            private boolean permissionRequired = false;

            @Comment
            @Comment("Enable this, if you want messages to be sent randomly")
            private boolean randomOrder = false;


            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            @SuppressWarnings("FieldMayBeFinal")
            @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
            public static class TitleNotificationMessageConfig extends OkaeriConfig {

                private Component title = converter.stringToComponent("&aExample title");

                private Component subtitle = converter.stringToComponent("&2Example subtitle");

            }

        }

    }

}

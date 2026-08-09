package ru.brikster.chatty.config.file;

import com.google.common.collect.Lists;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.validator.annotation.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import ru.brikster.chatty.BuildConstants;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.notification.advancement.AdvancementFrame;

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

    @Comment
    @Comment(value = {
            "Toast notifications: the pop-up the game shows for an advancement.",
            "Needs Minecraft 1.12 or newer; on older servers the section is ignored.",
            "Toast text is baked into the advancement when the plugin starts,",
            "so it is the same for everybody and cannot use placeholders."}, language = "en-US")
    @Comment(value = {
            "Всплывающие уведомления — та самая плашка, что показывается за достижение.",
            "Нужен Minecraft 1.12 или новее; на старых серверах секция игнорируется.",
            "Текст запекается в достижение при запуске плагина,",
            "поэтому он одинаков для всех и не поддерживает плейсхолдеры."}, language = "ru-RU")
    private AdvancementNotificationsConfig advancements = new AdvancementNotificationsConfig();

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
                    Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f),
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
            @Comment(value = "Time in seconds for periodically broadcasting", language = "en-US")
            @Comment(value = "Период рассылки в секундах", language = "ru-RU")
            private int period = 60;

            @Comment
            @Comment(value = {
                    "Messages of chat notifications support MiniMessage format BOTH",
                    "legacy color codes with &, various hex-codes formats.",
                    "You can use convenient WebUI: https://webui.advntr.dev/"}, language = "en-US")
            @Comment(value = {
                    "Сообщения уведомлений в чат поддерживают формат MiniMessage,",
                    "а также старые цветовые коды с & и разные форматы hex.",
                    "Удобный редактор: https://webui.advntr.dev/"}, language = "ru-RU")
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
            @Comment(value = "Enable this, if you want to play a sound along with the message", language = "en-US")
            @Comment(value = "Включите, если хотите проигрывать звук вместе с сообщением", language = "ru-RU")
            private boolean playSound = false;

            // Must not be the last field: okaeri fails to write a config whose
            // final entry is a nested section. ConfigGenerationTest guards this.
            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            @Comment
            @Comment(value = "Enable this, if you want to restrict channel by permission", language = "en-US")
            @Comment(value = "Включите, если хотите ограничить канал правом", language = "ru-RU")
            private boolean permissionRequired = false;

            @Comment
            @Comment(value = "Enable this, if you want messages to be sent randomly", language = "en-US")
            @Comment(value = "Включите, если хотите отправлять сообщения в случайном порядке", language = "ru-RU")
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
            @Comment(value = "Time in seconds for periodically broadcasting", language = "en-US")
            @Comment(value = "Период рассылки в секундах", language = "ru-RU")
            private int period = 60;

            @Positive
            @Comment
            @Comment(value = "Time in seconds for message stay", language = "en-US")
            @Comment(value = "Сколько секунд сообщение висит на экране", language = "ru-RU")
            @Comment(value = "Should be equal or lower than period", language = "en-US")
            @Comment(value = "Должно быть не больше периода", language = "ru-RU")
            private int stay = 60;

            @Comment
            @Comment(value = {
                    "Messages of chat notifications support MiniMessage format BOTH",
                    "legacy color codes with &, various hex-codes formats.",
                    "You can use convenient WebUI: https://webui.advntr.dev/"}, language = "en-US")
            @Comment(value = {
                    "Сообщения уведомлений в чат поддерживают формат MiniMessage,",
                    "а также старые цветовые коды с & и разные форматы hex.",
                    "Удобный редактор: https://webui.advntr.dev/"}, language = "ru-RU")
            private List<Component> messages = Lists.newArrayList(
                    converter.stringToComponent("&aFirst message from actionbar"),
                    converter.stringToComponent("&2Second message from actionbar")
            );

            @Comment
            @Comment(value = "Enable this, if you want to play a sound when the message appears", language = "en-US")
            @Comment(value = "Включите, если хотите проигрывать звук при появлении сообщения", language = "ru-RU")
            private boolean playSound = false;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            @Comment
            @Comment(value = "Enable this, if you want to restrict channel by permission", language = "en-US")
            @Comment(value = "Включите, если хотите ограничить канал правом", language = "ru-RU")
            private boolean permissionRequired = false;

            @Comment
            @Comment(value = "Enable this, if you want messages to be sent randomly", language = "en-US")
            @Comment(value = "Включите, если хотите отправлять сообщения в случайном порядке", language = "ru-RU")
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
            @Comment(value = "Time in seconds for periodically broadcasting", language = "en-US")
            @Comment(value = "Период рассылки в секундах", language = "ru-RU")
            private int period = 60;

            @Comment
            @Comment(value = {
                    "Messages of title notifications support MiniMessage format BOTH",
                    "legacy color codes with &, various hex-codes formats.",
                    "You can use convenient WebUI: https://webui.advntr.dev/"}, language = "en-US")
            @Comment(value = {
                    "Сообщения уведомлений-заголовков поддерживают формат MiniMessage,",
                    "а также старые цветовые коды с & и разные форматы hex.",
                    "Удобный редактор: https://webui.advntr.dev/"}, language = "ru-RU")
            private List<TitleNotificationMessageConfig> messages = Lists.newArrayList(new TitleNotificationMessageConfig());

            @Comment
            @Comment(value = "Enable this, if you want to play a sound along with the title", language = "en-US")
            @Comment(value = "Включите, если хотите проигрывать звук вместе с заголовком", language = "ru-RU")
            private boolean playSound = false;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            @Comment
            @Comment(value = "Enable this, if you want to restrict channel by permission", language = "en-US")
            @Comment(value = "Включите, если хотите ограничить канал правом", language = "ru-RU")
            private boolean permissionRequired = false;

            @Comment
            @Comment(value = "Enable this, if you want messages to be sent randomly", language = "en-US")
            @Comment(value = "Включите, если хотите отправлять сообщения в случайном порядке", language = "ru-RU")
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

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class AdvancementNotificationsConfig extends OkaeriConfig {

        private boolean enable = false;

        private Map<String, AdvancementNotificationChannelConfig> lists = new LinkedHashMap<>() {{
            put("default", new AdvancementNotificationChannelConfig());
        }};

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class AdvancementNotificationChannelConfig extends OkaeriConfig {

            @Positive
            @Comment(value = "Time in seconds for periodically broadcasting", language = "en-US")
            @Comment(value = "Период рассылки в секундах", language = "ru-RU")
            private int period = 600;

            @Comment
            @Comment(value = {
                    "A toast fits about two short lines, so keep the text brief.",
                    "\"icon\" is any item id, \"frame\" is TASK, GOAL or CHALLENGE"}, language = "en-US")
            @Comment(value = {
                    "В плашку помещается примерно две короткие строки, пишите кратко.",
                    "\"icon\" — любой предмет, \"frame\" — TASK, GOAL или CHALLENGE"}, language = "ru-RU")
            private List<AdvancementNotificationMessageConfig> messages =
                    Lists.newArrayList(new AdvancementNotificationMessageConfig());

            @Comment
            @Comment(value = "Enable this, if you want to play a sound along with the toast", language = "en-US")
            @Comment(value = "Включите, если хотите проигрывать звук вместе с плашкой", language = "ru-RU")
            private boolean playSound = false;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            @Comment
            @Comment(value = "Enable this, if you want to restrict channel by permission", language = "en-US")
            @Comment(value = "Включите, если хотите ограничить канал правом", language = "ru-RU")
            private boolean permissionRequired = false;

            @Comment
            @Comment(value = "Enable this, if you want messages to be sent randomly", language = "en-US")
            @Comment(value = "Включите, если хотите отправлять сообщения в случайном порядке", language = "ru-RU")
            private boolean randomOrder = false;

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            @SuppressWarnings("FieldMayBeFinal")
            @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
            public static class AdvancementNotificationMessageConfig extends OkaeriConfig {

                private Component title = converter.stringToComponent("&6Example toast");

                private Component description = converter.stringToComponent("&7Example description");

                private String icon = "minecraft:diamond";

                private AdvancementFrame frame = AdvancementFrame.TASK;

            }

        }

    }

}

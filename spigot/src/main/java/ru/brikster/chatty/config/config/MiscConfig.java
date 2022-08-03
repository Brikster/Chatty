package ru.brikster.chatty.config.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import lombok.Getter;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class MiscConfig extends OkaeriConfig {

    @Exclude
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private VanillaConfig vanilla = new VanillaConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class VanillaConfig extends OkaeriConfig {

        private JoinVanillaConfig join = new JoinVanillaConfig();

        @Getter
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class JoinVanillaConfig extends OkaeriConfig {

            @Comment("Use this, if you want completely disable feature")
            private boolean enable = true;

            @Comment
            @Comment("Set this to '', if you want to hide join message")
            private Component message = MINI_MESSAGE.deserialize("<green>* <yellow><player> joined the server.");

            @Comment
            @Comment("Play sound on join?")
            private boolean useSound = true;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            @Comment
            @Comment("If false, permission is not required to show join message.")
            @Comment("Otherwise, add permission: chatty.misc.joinmessage") // TODO check permission
            private boolean permissionRequired = false;

            @Comment
            private FirstJoinVanillaConfig firstJoin = new FirstJoinVanillaConfig();

            @Getter
            @SuppressWarnings("FieldMayBeFinal")
            @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
            public static class FirstJoinVanillaConfig extends OkaeriConfig {

                @Comment("Disable this, if you don't want to specify first join message")
                private boolean enable = true;

                @Comment
                @Comment("Set this to '', if you want to hide first join message")
                private Component message = MINI_MESSAGE.deserialize("<green>* <yellow><player> joined the server first time!");

                @Comment
                @Comment("Play sound on first join? If common join uses sound, it will be played")
                private boolean useSound = false;

                private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            }

        }

        @Comment
        private QuitVanillaConfig quit = new QuitVanillaConfig();

        @Getter
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class QuitVanillaConfig extends OkaeriConfig {

            @Comment("Use this, if you want completely disable feature")
            private boolean enable = true;

            @Comment
            @Comment("Set this to '', if you want to hide quit message")
            private Component message = MINI_MESSAGE.deserialize("<red>* <yellow><player> left the server.");

            @Comment
            @Comment("Play sound on quit?")
            private boolean useSound = true;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

            @Comment
            @Comment("If false, permission is not required to show quit message.")
            @Comment("Otherwise, add permission: chatty.misc.quitmessage") // TODO check permission
            private boolean permissionRequired = false;

        }

    }

}

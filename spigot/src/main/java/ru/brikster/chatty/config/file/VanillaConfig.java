package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
public class VanillaConfig extends OkaeriConfig {

    @Exclude
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Comment
    private JoinVanillaConfig join = new JoinVanillaConfig();

    @Comment
    private QuitVanillaConfig quit = new QuitVanillaConfig();

    @Comment
    private DeathVanillaConfig death = new DeathVanillaConfig();

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class JoinVanillaConfig extends OkaeriConfig {

        @Comment(value = "Use this, if you want completely disable feature", language = "en-US")
        @Comment(value = "Выключите, если хотите полностью отключить эту возможность", language = "ru-RU")
        private boolean enable = true;

        @Comment
        @Comment(value = "Set this to '', if you want to hide join message", language = "en-US")
        @Comment(value = "Поставьте '', если хотите скрыть сообщение о входе", language = "ru-RU")
        private Component message = MINI_MESSAGE.deserialize("<green>* <yellow>{player} joined the server.");

        @Comment
        @Comment(value = "Play sound on join?", language = "en-US")
        @Comment(value = "Проигрывать звук при входе?", language = "ru-RU")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment
        @Comment(value = "If false, permission is not required to show join message.", language = "en-US")
        @Comment(value = "Если false, право для показа сообщения о входе не нужно.", language = "ru-RU")
        @Comment(value = "Otherwise, add permission: chatty.misc.joinmessage", language = "en-US")
        @Comment(value = "Иначе выдайте право: chatty.misc.joinmessage", language = "ru-RU")
        private boolean permissionRequired = false;

        @Comment
        private FirstJoinVanillaConfig firstJoin = new FirstJoinVanillaConfig();

        @Getter
        @SuppressWarnings("FieldMayBeFinal")
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class FirstJoinVanillaConfig extends OkaeriConfig {

            @Comment(value = "Disable this, if you don't want to specify first join message", language = "en-US")
            @Comment(value = "Выключите, если не хотите задавать сообщение о первом входе", language = "ru-RU")
            private boolean enable = true;

            @Comment
            @Comment(value = "Set this to '', if you want to hide first join message", language = "en-US")
            @Comment(value = "Поставьте '', если хотите скрыть сообщение о первом входе", language = "ru-RU")
            private Component message = MINI_MESSAGE.deserialize("<green>* <yellow>{player} joined the server for the first time!");

            @Comment
            @Comment(value = "Disable this, if you don't want to specify first join sound", language = "en-US")
            @Comment(value = "Выключите, если не хотите задавать звук первого входа", language = "ru-RU")
            private boolean playSound = false;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        }

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class QuitVanillaConfig extends OkaeriConfig {

        @Comment(value = "Use this, if you want completely disable feature", language = "en-US")
        @Comment(value = "Выключите, если хотите полностью отключить эту возможность", language = "ru-RU")
        private boolean enable = true;

        @Comment
        @Comment(value = "Set this to '', if you want to hide quit message", language = "en-US")
        @Comment(value = "Поставьте '', если хотите скрыть сообщение о выходе", language = "ru-RU")
        private Component message = MINI_MESSAGE.deserialize("<red>* <yellow>{player} left the server.");

        @Comment
        @Comment(value = "Play sound on quit?", language = "en-US")
        @Comment(value = "Проигрывать звук при выходе?", language = "ru-RU")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment
        @Comment(value = "If false, permission is not required to show quit message.", language = "en-US")
        @Comment(value = "Если false, право для показа сообщения о выходе не нужно.", language = "ru-RU")
        @Comment(value = "Otherwise, add permission: chatty.misc.quitmessage", language = "en-US")
        @Comment(value = "Иначе выдайте право: chatty.misc.quitmessage", language = "ru-RU")
        private boolean permissionRequired = false;

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class DeathVanillaConfig extends OkaeriConfig {

        @Comment(value = "Use this, if you want completely disable feature", language = "en-US")
        @Comment(value = "Выключите, если хотите полностью отключить эту возможность", language = "ru-RU")
        private boolean enable = true;

        @Comment
        @Comment(value = "Use {cause} placeholder; the texts for it live in the lang file.", language = "en-US")
        @Comment(value = "Используйте плейсхолдер {cause}; тексты для него лежат в языковом файле.", language = "ru-RU")
        @Comment(value = "Set this to '', if you want to hide death message", language = "en-US")
        @Comment(value = "Поставьте '', если хотите скрыть сообщение о смерти", language = "ru-RU")
        private Component message = MINI_MESSAGE.deserialize("<red>* <yellow>{player} {cause}.");

        @Comment
        @Comment(value = "Play sound on death?", language = "en-US")
        @Comment(value = "Проигрывать звук при смерти?", language = "ru-RU")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment
        @Comment(value = "If false, permission is not required to show death message.", language = "en-US")
        @Comment(value = "Если false, право для показа сообщения о смерти не нужно.", language = "ru-RU")
        @Comment(value = "Otherwise, add permission: chatty.misc.deathmessage", language = "en-US")
        @Comment(value = "Иначе выдайте право: chatty.misc.deathmessage", language = "ru-RU")
        private boolean permissionRequired = false;

    }

}

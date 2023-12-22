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

import java.util.HashMap;
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

        @Comment("Use this, if you want completely disable feature")
        private boolean enable = true;

        @Comment
        @Comment("Set this to '', if you want to hide join message")
        private Component message = MINI_MESSAGE.deserialize("<green>* <yellow>{player} joined the server.");

        @Comment
        @Comment("Play sound on join?")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment
        @Comment("If false, permission is not required to show join message.")
        @Comment("Otherwise, add permission: chatty.misc.joinmessage")
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
            private Component message = MINI_MESSAGE.deserialize("<green>* <yellow>{player} joined the server for the first time!");

            @Comment
            @Comment("Disable this, if you don't want to specify first join sound")
            private boolean playSound = false;

            private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        }

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class QuitVanillaConfig extends OkaeriConfig {

        @Comment("Use this, if you want completely disable feature")
        private boolean enable = true;

        @Comment
        @Comment("Set this to '', if you want to hide quit message")
        private Component message = MINI_MESSAGE.deserialize("<red>* <yellow>{player} left the server.");

        @Comment
        @Comment("Play sound on quit?")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment
        @Comment("If false, permission is not required to show quit message.")
        @Comment("Otherwise, add permission: chatty.misc.quitmessage")
        private boolean permissionRequired = false;

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class DeathVanillaConfig extends OkaeriConfig {

        @Comment("Use this, if you want completely disable feature")
        private boolean enable = true;

        @Comment
        @Comment("Use {cause} placeholder and \"causes\" for custom causes handling.")
        @Comment("Set this to '', if you want to hide death message")
        private Component message = MINI_MESSAGE.deserialize("<red>* <yellow>{player} {cause}.");

        @Comment
        @Comment("Texts for the death causes can be configured.")
        @Comment("See keys for \"causes\" section here: ")
        @Comment("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html")
        private String fallbackCause = "killed by something strange";
        private Map<String, String> causes = new HashMap<>() {{
            put("BLOCK_EXPLOSION", "caught in block explosion");
            put("CONTACT", "pricked by cactus, stalagmite, or berry bush");
            put("CRAMMING", "crammed by too many entities");
            put("DRAGON_BREATH", "harmed by dragon's breath");
            put("DROWNING", "drowned underwater");
            put("DRYOUT", "dried out outside water");
            put("ENTITY_ATTACK", "attacked by an entity");
            put("ENTITY_EXPLOSION", "caught in entity explosion");
            put("ENTITY_SWEEP_ATTACK", "hit by sweep attack");
            put("FALL", "fell from a height");
            put("FALLING_BLOCK", "hit by a falling block");
            put("FIRE", "burned in fire");
            put("FIRE_TICK", "suffered from fire burns");
            put("FLY_INTO_WALL", "flew into a wall");
            put("FREEZE", "froze to death");
            put("HOT_FLOOR", "stepped on a hot floor");
            put("LAVA", "swam in lava");
            put("LIGHTNING", "struck by lightning");
            put("MAGIC", "hit by a magic potion or spell");
            put("MELTING", "melted away");
            put("POISON", "poisoned");
            put("PROJECTILE", "hit by a projectile");
            put("SONIC_BOOM", "hit by Warden's sonic boom");
            put("STARVATION", "starved to death");
            put("SUFFOCATION", "suffocated in a block");
            put("SUICIDE", "committed suicide");
            put("THORNS", "harmed by Thorns enchantment");
            put("VOID", "fell into the void");
            put("WITHER", "withered away");
            put("CUSTOM", "killed by something strange");
        }};

        @Comment
        @Comment("Play sound on death?")
        private boolean playSound = true;

        private Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Source.MASTER, 1f, 1f);

        @Comment
        @Comment("If false, permission is not required to show death message.")
        @Comment("Otherwise, add permission: chatty.misc.deathmessage")
        private boolean permissionRequired = false;

    }

}

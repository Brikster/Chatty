package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
        private Map<DamageCause, String> causes = new HashMap<>() {{
            put(DamageCause.BLOCK_EXPLOSION, "caught in block explosion");
            put(DamageCause.CONTACT, "pricked by cactus, stalagmite, or berry bush");
            put(DamageCause.CRAMMING, "crammed by too many entities");
            put(DamageCause.DRAGON_BREATH, "harmed by dragon's breath");
            put(DamageCause.DROWNING, "drowned underwater");
            put(DamageCause.DRYOUT, "dried out outside water");
            put(DamageCause.ENTITY_ATTACK, "attacked by an entity");
            put(DamageCause.ENTITY_EXPLOSION, "caught in entity explosion");
            put(DamageCause.ENTITY_SWEEP_ATTACK, "hit by sweep attack");
            put(DamageCause.FALL, "fell from a height");
            put(DamageCause.FALLING_BLOCK, "hit by a falling block");
            put(DamageCause.FIRE, "burned in fire");
            put(DamageCause.FIRE_TICK, "suffered from fire burns");
            put(DamageCause.FLY_INTO_WALL, "flew into a wall");
            put(DamageCause.FREEZE, "froze to death");
            put(DamageCause.HOT_FLOOR, "stepped on a hot floor");
            put(DamageCause.LAVA, "swam in lava");
            put(DamageCause.LIGHTNING, "struck by lightning");
            put(DamageCause.MAGIC, "hit by a magic potion or spell");
            put(DamageCause.MELTING, "melted away");
            put(DamageCause.POISON, "poisoned");
            put(DamageCause.PROJECTILE, "hit by a projectile");
            put(DamageCause.SONIC_BOOM, "hit by Warden's sonic boom");
            put(DamageCause.STARVATION, "starved to death");
            put(DamageCause.SUFFOCATION, "suffocated in a block");
            put(DamageCause.SUICIDE, "committed suicide");
            put(DamageCause.THORNS, "harmed by Thorns enchantment");
            put(DamageCause.VOID, "fell into the void");
            put(DamageCause.WITHER, "withered away");
            put(DamageCause.CUSTOM, "killed by something strange");
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

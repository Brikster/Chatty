package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.brikster.chatty.BuildConstants;

import java.util.LinkedHashMap;
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
public class MessagesConfig extends OkaeriConfig {

    @Exclude
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Comment({"", "Common messages"})
    private Component chatNotFound = MINI_MESSAGE.deserialize("<red>No matching chat found. Maybe you don't have enough permissions?");
    private Component nobodyHeard = MINI_MESSAGE.deserialize("<red>Nobody heard you.");
    private Component waitCooldown = MINI_MESSAGE.deserialize("<red>Wait {secondsLeft} sec. before next message.");
    private Component chatErrorOccurred = MINI_MESSAGE.deserialize("<red>An error occurred while processing your message. Please contact the server administrator.");

    // Moderation methods
    @Comment({"", "Messages for moderation"})
    private Component advertisementFound = MINI_MESSAGE.deserialize("<red>Advertisement found in your message.");
    private Component capsFound = MINI_MESSAGE.deserialize("<red>Caps violations found in your message.");
    private Component swearFound = MINI_MESSAGE.deserialize("<red>Swear found in your message.");

    // Private messages
    @Comment({"", "Messages for PM"})
    private Component pmPlayerNotFound = MINI_MESSAGE.deserialize("<red>Player not found.");
    private Component pmNobodyToReply = MINI_MESSAGE.deserialize("<red>Nobody to reply.");
    private Component pmCannotPmYourself = MINI_MESSAGE.deserialize("<red>You cannot PM yourself.");
    private Component pmYouNowIgnore = MINI_MESSAGE.deserialize("<green>You now ignore this player.");
    private Component pmCannotIgnoreYourself = MINI_MESSAGE.deserialize("<red>You cannot ignore yourself.");
    private Component pmYouAlreadyIgnore = MINI_MESSAGE.deserialize("<red>You already ignore this player.");
    private Component pmYouDontNowIgnore = MINI_MESSAGE.deserialize("<green>You now don't ignore this player.");
    private Component pmYouDontIgnore = MINI_MESSAGE.deserialize("<red>You don't ignore this player.");
    private Component pmIgnoreList = MINI_MESSAGE.deserialize("<green>Ignore list: {players}.");

    // Death causes
    @Comment({"", "Texts substituted into {cause} of the death message in vanilla.yml.",
            "Keys are Bukkit damage causes, see:",
            "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html"})
    private String deathFallbackCause = "killed by something strange";
    private Map<String, String> deathCauses = new LinkedHashMap<>() {{
        put("BLOCK_EXPLOSION", "caught in block explosion");
        put("CONTACT", "pricked by cactus, stalagmite, or berry bush");
        put("CRAMMING", "crammed by too many entities");
        put("CUSTOM", "killed by something strange");
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
    }};

    // Commands
    @Comment({"", "Messages for commands"})
    private Component cmdArgumentParsingError = MINI_MESSAGE.deserialize("<red>Invalid command argument: {argument}.");
    private Component cmdUsageError = MINI_MESSAGE.deserialize("<red>Usage: {usage}.");
    private Component cmdSenderTypeError = MINI_MESSAGE.deserialize("<red>You cannot use this command.");
    private Component cmdNoPermissionError = MINI_MESSAGE.deserialize("<red>You don't have permission.");
    private Component cmdExecutionError = MINI_MESSAGE.deserialize("<red>Error while command execution, report it to the server admin.");

    private Component reloadCommandSuccess = MINI_MESSAGE.deserialize("<green>Plugin successfully reloaded!");
    private Component clearchatCommandSuccess = MINI_MESSAGE.deserialize("<green>Chat cleared.");
    private Component clearchatCommandClearedForAll = MINI_MESSAGE.deserialize("<green>Chat cleared by {player}.");
    private Component muted = MINI_MESSAGE.deserialize("<red>You are muted {duration}. {reason}");
    private String mutePermanently = "permanently";
    private String muteDurationDays = "{days}d {hours}h";
    private String muteDurationHours = "{hours}h {minutes}m";
    private String muteDurationMinutes = "{minutes}m {seconds}s";
    private String muteDurationSeconds = "{seconds}s";
    private Component muteCommandSuccess = MINI_MESSAGE.deserialize("<green>{player} has been muted {duration}.");
    private Component muteCommandUnmuted = MINI_MESSAGE.deserialize("<green>{player} is no longer muted.");
    private Component muteCommandNotMuted = MINI_MESSAGE.deserialize("<red>{player} is not muted.");
    private Component chatCommandSwitched = MINI_MESSAGE.deserialize("<green>You are now writing in {chat}.");
    private Component spyCommandSpyIsNowEnabled = MINI_MESSAGE.deserialize("<green>Spy mode is now enabled.");
    private Component spyCommandSpyIsNowDisabled = MINI_MESSAGE.deserialize("<red>Spy mode is now disabled.");

}

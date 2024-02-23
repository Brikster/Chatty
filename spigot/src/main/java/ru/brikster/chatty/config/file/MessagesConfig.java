package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
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
public class MessagesConfig extends OkaeriConfig {

    @Exclude
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Comment({"", "Common messages"})
    private Component chatNotFound = MINI_MESSAGE.deserialize("<red>No matching chat found. Maybe you don't have enough permissions?");
    private Component nobodyHeard = MINI_MESSAGE.deserialize("<red>Nobody heard you.");
    private Component waitCooldown = MINI_MESSAGE.deserialize("<red>Wait {secondsLeft} sec. before next message.");

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

    // Commands
    @Comment({"", "Messages for commands"})
    private Component cmdArgumentParsingError = MINI_MESSAGE.deserialize("<red>Invalid command argument: {argument}.");
    private Component cmdUsageError = MINI_MESSAGE.deserialize("<red>Usage: {usage}.");
    private Component cmdSenderTypeError = MINI_MESSAGE.deserialize("<red>You cannot use this command.");
    private Component cmdNoPermissionError = MINI_MESSAGE.deserialize("<red>You don't have permission.");
    private Component cmdExecutionError = MINI_MESSAGE.deserialize("<red>Error while command execution, report it to the server admin.");

    private Component reloadCommandSuccess = MINI_MESSAGE.deserialize("<green>Plugin successfully reloaded!");
    private Component clearchatCommandSuccess = MINI_MESSAGE.deserialize("<green>Chat cleared.");

    @Comment({"", "Messages for spy"})
    private Component spyEnabled = MINI_MESSAGE.deserialize("<green>Spy enabled.");
    private Component spyDisabled = MINI_MESSAGE.deserialize("<red>Spy disabled.");

}

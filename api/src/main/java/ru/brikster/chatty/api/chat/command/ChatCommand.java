package ru.brikster.chatty.api.chat.command;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ChatCommand {

    /**
     * Command used to write chat with /{command} {message}
     *
     * @return name of chat command
     */
    @NotNull String getName();

    /**
     * Chat command can have aliases
     *
     * @return string set of aliases
     */
    @NotNull Set<String> getAliases();

    /**
     * Allows to switch chat with /{command}
     *
     * @return can chat be completely switched with command
     */
    boolean isCanSwitchWithCommand();

    /**
     * If true, player will be added to recipients only when
     * switched to the chat.
     * Used both with {@link ChatCommand#isCanSwitchWithCommand}
     *
     * @return is chat read only when switched
     */
    boolean isReadOnlySwitched();

}

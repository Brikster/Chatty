package ru.brikster.chatty.chat.command;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.command.ChatCommand;

import java.util.Set;

@Value
public class ChatCommandImpl implements ChatCommand {

    @NotNull String name;
    @NotNull Set<String> aliases;
    boolean canSwitchWithCommand;
    boolean readOnlySwitched;

}

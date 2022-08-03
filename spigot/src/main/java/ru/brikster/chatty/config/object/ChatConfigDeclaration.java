package ru.brikster.chatty.config.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatConfigDeclaration {

    private @NotNull String displayName;
    private @NotNull String format;
    private @NotNull String symbol;
    private int range;
    private int cooldown;
    private @Nullable Integer vaultPrice;
    private boolean permissionRequired;
    private boolean notifyNobodyHeard;
    private @Nullable ChatCommandConfigDeclaration command;

    @Getter
    @AllArgsConstructor
    public static class ChatCommandConfigDeclaration {

        private String name;
        private List<String> aliases;
        private boolean allowSwitchWithCommand;
        private boolean readOnlySwitched;

    }

}

package ru.mrbrikster.chatty.moderation;

public abstract class ModifyingSubstringsModerationMethod extends ModerationMethod {

    protected final String lastFormatColors;

    ModifyingSubstringsModerationMethod(String message, String lastFormatColors) {
        super(message);

        this.lastFormatColors = lastFormatColors;
    }

}

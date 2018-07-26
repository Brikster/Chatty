package ru.mrbrikster.chatty.moderation;

public abstract class ModerationMethod {

    protected final String message;

    ModerationMethod(String message) {
        this.message = message;
    }

    public abstract String getEditedMessage();

    public abstract boolean isPassed();

}

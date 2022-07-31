package ru.brikster.chatty.chat.selection;

import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;

import java.util.function.Predicate;

public interface ChatSelector {

    @Nullable Chat selectChat(String message, Predicate<Chat> allowedPredicate);

}

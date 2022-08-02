package ru.brikster.chatty.chat.selection;

import com.google.inject.Inject;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;

import java.util.function.Predicate;

public class ChatSelectorImpl implements ChatSelector {

    @Inject
    private ChatRegistry registry;

    @Override
    public @Nullable Chat selectChat(String message, Predicate<Chat> allowedPredicate) {
        // TODO maybe add chats priorities ?
        Chat selected = null;
        for (Chat chat : registry.getChats()) {
            if (!allowedPredicate.test(chat)) {
                continue;
            }

            if ((selected == null || selected.getSymbol().isEmpty()) && chat.getSymbol().isEmpty() || message.startsWith(chat.getSymbol())) {
                selected = chat;
            }
        }

        return selected;
    }

}

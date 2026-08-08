package ru.brikster.chatty.chat.selection;

import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Predicate;

@Singleton
public final class ChatSelectorImpl implements ChatSelector {

    @Inject
    private ChatRegistry registry;

    @Override
    public @Nullable Chat selectChat(String message, Predicate<Chat> allowedPredicate) {
        Chat selected = null;

        for (Chat chat : registry.getChats().values()) {
            if (!allowedPredicate.test(chat)) {
                continue;
            }

            String symbol = chat.getSymbol();
            if (!symbol.isEmpty() && !message.startsWith(symbol)) {
                continue;
            }

            if (selected == null || isCloserMatch(chat, selected)) {
                selected = chat;
            }
        }

        return selected;
    }

    private static boolean isCloserMatch(Chat candidate, Chat current) {
        int lengthDifference = candidate.getSymbol().length() - current.getSymbol().length();
        if (lengthDifference != 0) {
            return lengthDifference > 0;
        }
        return candidate.getId().compareTo(current.getId()) < 0;
    }

}

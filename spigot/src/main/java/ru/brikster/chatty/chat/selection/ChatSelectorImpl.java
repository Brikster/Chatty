package ru.brikster.chatty.chat.selection;

import org.bukkit.entity.Player;
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

    @Inject
    private ChatSelectionState selectionState;

    @Override
    public @Nullable Chat selectChat(Player sender, String message, Predicate<Chat> allowedPredicate) {
        Chat pending = sender == null ? null
                : allowed(selectionState.takePendingChat(sender.getUniqueId()), allowedPredicate);
        if (pending != null) {
            return pending;
        }

        Chat bySymbol = null;
        Chat withoutSymbol = null;

        for (Chat chat : registry.getChats().values()) {
            if (!allowedPredicate.test(chat)) {
                continue;
            }

            if (chat.getSymbol().isEmpty()) {
                if (withoutSymbol == null || isCloserMatch(chat, withoutSymbol)) {
                    withoutSymbol = chat;
                }
            } else if (message.startsWith(chat.getSymbol())
                    && (bySymbol == null || isCloserMatch(chat, bySymbol))) {
                bySymbol = chat;
            }
        }

        if (bySymbol != null) {
            return bySymbol;
        }

        Chat switched = sender == null ? null
                : allowed(selectionState.getSwitchedChat(sender.getUniqueId()), allowedPredicate);
        return switched != null ? switched : withoutSymbol;
    }

    private @Nullable Chat allowed(@Nullable String chatId, Predicate<Chat> allowedPredicate) {
        if (chatId == null) {
            return null;
        }
        Chat chat = registry.getChats().get(chatId);
        return chat != null && allowedPredicate.test(chat) ? chat : null;
    }

    private static boolean isCloserMatch(Chat candidate, Chat current) {
        int lengthDifference = candidate.getSymbol().length() - current.getSymbol().length();
        if (lengthDifference != 0) {
            return lengthDifference > 0;
        }
        return candidate.getId().compareTo(current.getId()) < 0;
    }

}

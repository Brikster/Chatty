package ru.brikster.chatty.chat.selection;

import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Singleton
public final class ChatSelectorImpl implements ChatSelector {

    @Inject
    private ChatRegistry registry;

    @Override
    public @Nullable Chat selectChat(String message, Predicate<Chat> allowedPredicate) {
        // TODO maybe add chats priorities ?
        List<Chat> chats = new ArrayList<>(registry.getChats().values());
        chats.sort(Comparator.comparing(Chat::getId));

        Chat selected = null;
        Chat fallback = null;
        int selectedSymbolLength = -1;

        for (Chat chat : chats) {
            if (!allowedPredicate.test(chat)) {
                continue;
            }

            String symbol = chat.getSymbol();
            if (symbol.isEmpty()) {
                if (fallback == null) {
                    fallback = chat;
                }
                continue;
            }

            if (message.startsWith(symbol) && symbol.length() > selectedSymbolLength) {
                selected = chat;
                selectedSymbolLength = symbol.length();
            }
        }

        return selected != null ? selected : fallback;
    }

}

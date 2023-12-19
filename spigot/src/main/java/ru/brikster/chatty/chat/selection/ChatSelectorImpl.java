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
        // TODO maybe add chats priorities ?
        Chat selected = null;
        for (Chat chat : registry.getChats().values()) {
            if (!allowedPredicate.test(chat)) {
                continue;
            }

            if (chat.getSymbol().isEmpty()
                    && (selected == null || selected.getSymbol().isEmpty())) {
                selected = chat;
                continue;
            }

            if (!chat.getSymbol().isEmpty() && message.startsWith(chat.getSymbol())) {
                selected = chat;
            }
        }

        return selected;
    }

}

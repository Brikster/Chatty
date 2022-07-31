package ru.brikster.chatty.chat.selection;

import com.google.inject.Inject;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.registry.ChatRegistry;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ChatSelectorImpl implements ChatSelector {

    @Inject
    private ChatRegistry registry;

    @Override
    public @Nullable Chat selectChat(String message, Predicate<Chat> allowedPredicate) {
        System.out.println("Chats: " + registry.getChats().stream().map(chat -> chat.getName()).collect(Collectors.joining(", ")));
        // TODO maybe add chats priorities ?
        Chat selected = null;
        for (Chat chat : registry.getChats()) {
            if (!allowedPredicate.test(chat)) {
                continue;
            }

            if (chat.getSymbol().isEmpty() || message.startsWith(chat.getSymbol())) {
                selected = chat;
                break;
            }
        }

        return selected;
    }

}

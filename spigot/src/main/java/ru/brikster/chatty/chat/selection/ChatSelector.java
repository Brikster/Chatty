package ru.brikster.chatty.chat.selection;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.api.chat.Chat;

import java.util.function.Predicate;

public interface ChatSelector {

    @Nullable Chat selectChat(Player sender, String message, Predicate<Chat> allowedPredicate);

}

package ru.brikster.chatty.api.chat.handle.strategy;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.api.chat.handle.context.MessageContext;

import java.util.Collection;

public interface MessageTransformStrategy<F, T> {

    @NotNull Result<T> handle(MessageContext<F> context);

    @NotNull Stage getStage();

    enum Stage {
        EARLY(String.class),
        LATE(Component.class),
        POST(Component.class);;

        private final Class<?> finalTransformClazz;

        Stage(Class<?> finalTransformClazz) {
            this.finalTransformClazz = finalTransformClazz;
        }

        public Class<?> getFinalTransformClass() {
            return finalTransformClazz;
        }
    }

    interface Result<T> {

        default boolean wasUpdated() {
            return getAddedRecipients().size() != 0
                    || getRemovedRecipients().size() != 0
                    || isFormatUpdated()
                    || isMessageUpdated()
                    || isBecameCancelled();
        }

        @NotNull MessageContext<T> getNewContext();

        @NotNull
        Collection<? extends @NotNull Player> getAddedRecipients();

        @NotNull
        Collection<? extends @NotNull Player> getRemovedRecipients();

        boolean isFormatUpdated();

        boolean isMessageUpdated();

        boolean isBecameCancelled();

    }

}

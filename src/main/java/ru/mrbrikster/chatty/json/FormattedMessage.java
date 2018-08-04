package ru.mrbrikster.chatty.json;

import lombok.Getter;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.fanciful.FancyMessage;

import java.util.*;
import java.util.regex.Pattern;

public class FormattedMessage {

    @Getter private final List<MessagePart> messageParts
            = new ArrayList<>();

    public FormattedMessage(String text) {
        this.messageParts.add(new LegacyMessagePart(text));
    }

    public void send(Player player) {
        buildFancyMessage().send(player);
    }

    public void send(Collection<? extends Player> players) {
        buildFancyMessage().send(players);
    }

    public FormattedMessage append(MessagePart messagePart) {
        this.messageParts.add(messagePart);

        return this;
    }

    public FormattedMessage replace(String text, MessagePart messagePart) {
        Map<Integer, List<MessagePart>> replacements =
                new HashMap<>();

        for (int k = 0; k < messageParts.size(); k++) {
            MessagePart part = messageParts.get(k);
            if (!(part instanceof LegacyMessagePart))
                continue;

            String legacyText = ((LegacyMessagePart) part).getText();

            if (!legacyText.contains(text)) continue;

            List<MessagePart> updatedMessageParts = new ArrayList<>();
            String[] legacyTextSplit = legacyText.split(Pattern.quote(text));

            if (legacyTextSplit.length == 1)
                legacyTextSplit = new String[]{legacyTextSplit[0], ""};

            for (int i = 0; i < legacyTextSplit.length; i++) {
                String splitPart = legacyTextSplit[i];

                if (!splitPart.isEmpty())
                    updatedMessageParts.add(new LegacyMessagePart(splitPart));

                if (i != (legacyTextSplit.length - 1))
                    updatedMessageParts.add(messagePart);
            }

            replacements.put(k, updatedMessageParts);
        }

        for (Map.Entry<Integer, List<MessagePart>> integerListEntry : replacements.entrySet()) {
            int idx = integerListEntry.getKey();
            List<MessagePart> updatedMessageParts = integerListEntry.getValue();

            messageParts.remove(idx);
            messageParts.addAll(idx, updatedMessageParts);
        }

        return this;
    }

    private FancyMessage buildFancyMessage() {
        FancyMessage fancyMessage = new FancyMessage("");

        for (MessagePart messagePart : messageParts) {
            fancyMessage = messagePart.append(fancyMessage);
        }

        return fancyMessage;
    }

}

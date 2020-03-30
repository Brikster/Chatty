package ru.mrbrikster.chatty.json;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

import java.util.*;
import java.util.regex.Pattern;

public class FormattedMessage {

    private final List<MessagePart> messageParts
            = new ArrayList<>();

    public FormattedMessage(String text) {
        this.messageParts.add(new LegacyMessagePart(text));
    }

    public FormattedMessage send(Player player) {
        buildFancyMessage().send(player);

        return this;
    }

    public FormattedMessage send(Collection<? extends Player> players) {
        buildFancyMessage().send(players);

        return this;
    }

    public void sendConsole() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(buildFancyMessage().toOldMessageFormat()));
    }

    public FormattedMessage append(FormattedMessage formattedMessage) {
        this.messageParts.addAll(formattedMessage.messageParts);

        return this;
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
            String[] legacyTextSplit = legacyText.split(Pattern.quote(text), 2);

            if (legacyTextSplit.length == 1)
                legacyTextSplit = new String[] {legacyTextSplit[0], ""};

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

        fancyMessage.getMessageParts().removeIf(messagePart ->
            messagePart.text.toString().isEmpty()
        );

        return fancyMessage;
    }

    public String toReadableText() {
        return buildFancyMessage().toOldMessageFormat();
    }

    public String toJSONString() {
        return buildFancyMessage().toJSONString();
    }

}

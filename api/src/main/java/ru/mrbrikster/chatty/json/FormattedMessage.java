package ru.mrbrikster.chatty.json;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedMessage {

    private List<MessagePart> messageParts = new ArrayList<>();

    public FormattedMessage() {}

    public FormattedMessage(String text) {
        this.messageParts.add(new LegacyMessagePart(text));
    }

    public FormattedMessage send(Collection<? extends Player> players, UUID sender) {
        toFancyMessage().send(players, sender);

        return this;
    }

    public void sendConsole() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(toFancyMessage().toOldMessageFormat()));
    }

    public FormattedMessage append(FormattedMessage formattedMessage) {
        this.messageParts.addAll(formattedMessage.messageParts);

        return this;
    }

    public FormattedMessage append(MessagePart messagePart) {
        this.messageParts.add(messagePart);

        return this;
    }

    public FormattedMessage replace(Pattern pattern, FormattedMessage message) {
        return replace(pattern, message.messageParts);
    }

    public FormattedMessage replace(String text, FormattedMessage message) {
        return replace(Pattern.compile(Pattern.quote(text)), message.messageParts);
    }

    public FormattedMessage replace(String text, MessagePart... parts) {
        return replace(Pattern.compile(Pattern.quote(text)), Arrays.asList(parts));
    }

    public FormattedMessage replace(Pattern pattern, MessagePart... parts) {
        return replace(pattern, Arrays.asList(parts));
    }

    public FormattedMessage replace(String text, List<MessagePart> parts) {
        return replace(Pattern.compile(Pattern.quote(text)), parts);
    }

    /**
     * EXPERIMENTAL
     * Rewritten function, that supports multiple parts and should be more stable and effective
     *
     * @return this instance of FormattedMessage
     */
    public FormattedMessage replace(Pattern pattern, List<MessagePart> parts) {
        List<MessagePart> updatedMessageParts = new ArrayList<>();

        for (MessagePart messagePart : messageParts) {
            if (messagePart instanceof LegacyMessagePart) {
                LegacyMessagePart legacyPart = (LegacyMessagePart) messagePart;

                String partText = legacyPart.getText();
                Matcher matcher = pattern.matcher(partText);

                int firstIndex = 0;
                while (matcher.find()) {
                    updatedMessageParts.add(new LegacyMessagePart(partText.substring(firstIndex, matcher.start())));
                    updatedMessageParts.addAll(parts);
                    firstIndex = matcher.end();
                }

                String tail = partText.substring(firstIndex);

                if (!tail.isEmpty()) {
                    updatedMessageParts.add(new LegacyMessagePart(tail));
                }
            } else {
                updatedMessageParts.add(messagePart);
            }
        }

        this.messageParts = updatedMessageParts;

        return this;
    }

    /*
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
    */

    public String getLastColors() {
        return toFancyMessage().getLastColors();
    }

    public FancyMessage toFancyMessage() {
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
        return toFancyMessage().toOldMessageFormat();
    }

    public String toJSONString() {
        return toFancyMessage().toJSONString();
    }

}

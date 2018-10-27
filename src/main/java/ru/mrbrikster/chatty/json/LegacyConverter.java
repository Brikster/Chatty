package ru.mrbrikster.chatty.json;

import org.bukkit.ChatColor;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;
import ru.mrbrikster.chatty.json.fanciful.MessagePart;

import java.util.List;

class LegacyConverter {

    private boolean bold, italic, underline, strike, magic, lastCharSection = false;
    private StringBuilder builder = new StringBuilder();
    private ChatColor color;
    private FancyMessage fancyMessage = new FancyMessage();
    private boolean first = true;

    private LegacyConverter(String message) {
        for (char c : message.toCharArray()) {
            if (c == '§') {
                lastCharSection = true;
                continue;
            }

            if (lastCharSection) {
                lastCharSection = false;

                if (processFormatCodeContains(c)) {
                    continue;
                }

                ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                    finalizeSection();
                    this.color = color;

                    continue;
                }
            }

            builder.append(c);
        }

        finalizeSection();
    }

    private boolean processFormatCodeContains(char c) {
        switch (c) {
            case 'k':
                return (magic = true);
            case 'l':
                return (bold = true);
            case 'm':
                return (strike = true);
            case 'n':
                return (underline = true);
            case 'o':
                return (italic = true);
            case 'r':
                finalizeSection();
                color = null;
                return true;
        }

        return false;
    }

    private void finalizeSection() {
        if (first) fancyMessage.text(builder.toString());
        else fancyMessage.then(builder.toString());

        first = false;

        if (bold) fancyMessage.style(ChatColor.BOLD);
        if (strike) fancyMessage.style(ChatColor.STRIKETHROUGH);
        if (underline) fancyMessage.style(ChatColor.UNDERLINE);
        if (italic) fancyMessage.style(ChatColor.ITALIC);
        if (magic) fancyMessage.style(ChatColor.MAGIC);

        if (color != null) {
            fancyMessage.color(color);
        }

        magic = false;
        bold = false;
        strike = false;
        underline = false;
        italic = false;

        builder = new StringBuilder();
    }

    private List<MessagePart> toMessageParts() {
        return fancyMessage.getMessageParts();
    }

    static List<ru.mrbrikster.chatty.json.fanciful.MessagePart> getMessageParts(String message) {
        return new LegacyConverter(message).toMessageParts();
    }
}

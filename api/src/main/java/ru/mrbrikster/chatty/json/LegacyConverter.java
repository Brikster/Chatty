package ru.mrbrikster.chatty.json;

import net.md_5.bungee.api.ChatColor;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;
import ru.mrbrikster.chatty.json.fanciful.MessagePart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

class LegacyConverter {

    private final FancyMessage fancyMessage = new FancyMessage();

    private final String strippedString;
    private boolean url;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strike;
    private boolean magic;
    private StringBuilder builder = new StringBuilder();
    private ChatColor color;
    private boolean first = true;

    private LegacyConverter(String message) {
        strippedString = ChatColor.stripColor(message);
        try {
            new URL(strippedString);
            url = true;
        } catch (MalformedURLException e) {
            url = false;
        }

        String hex = "";
        boolean checkingForHex = false;
        boolean lastCharSection = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                if (lastCharSection && checkingForHex) {
                    checkingForHex = false;
                    hex = "";
                }

                lastCharSection = true;
                continue;
            } else if (checkingForHex) {
                lastCharSection = false;
                hex = hex + c;
            }

            if (hex.length() == 6) {
                finalizeSection();
                try {
                    this.color = ChatColor.of("#" + hex);
                } catch (Exception ignored) {}
                checkingForHex = false;
                hex = "";
                continue;
            } else if (checkingForHex) {
                continue;
            }

            if (lastCharSection) {
                lastCharSection = false;

                if (c == 'x') {
                    checkingForHex = true;
                    continue;
                }

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
        if (url) fancyMessage.link(strippedString);

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

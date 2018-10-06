package ru.mrbrikster.chatty.json;

import org.bukkit.ChatColor;
import ru.mrbrikster.chatty.fanciful.FancyMessage;

import java.util.List;

public class JSONMessagePart implements MessagePart {

    private final String text;
    private String command;
    private String suggest;
    private List<String> tooltip;

    public JSONMessagePart(String text) {
        this.text = text;
    }

    public JSONMessagePart command(String command) {
        this.command = command;

        return this;
    }

    public JSONMessagePart suggest(String suggest) {
        this.suggest = suggest;

        return this;
    }

    public JSONMessagePart tooltip(List<String> tooltip) {
        this.tooltip = tooltip;

        return this;
    }

    @Override
    public FancyMessage append(FancyMessage fancyMessage) {
        LegacyConverter.getMessageParts(fancyMessage.getLastColors() + ChatColor.translateAlternateColorCodes('&', text)).forEach(messagePart -> {
            fancyMessage.then(messagePart);

            if (command != null)
                fancyMessage.command(command);

            if (suggest != null)
                fancyMessage.suggest(suggest);

            if (tooltip != null
                    && !tooltip.isEmpty())
                fancyMessage.tooltip(tooltip);
        });

        return fancyMessage;
    }

}

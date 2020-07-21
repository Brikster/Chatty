package ru.mrbrikster.chatty.json;

import ru.mrbrikster.chatty.json.fanciful.FancyMessage;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.List;

public class JsonMessagePart implements MessagePart {

    private final String text;
    private String command;
    private String suggest;
    private List<String> tooltip;
    private String link;

    public JsonMessagePart(String text) {
        this.text = text;
    }

    public JsonMessagePart command(String command) {
        this.command = command;

        return this;
    }

    public JsonMessagePart suggest(String suggest) {
        this.suggest = suggest;

        return this;
    }

    public JsonMessagePart tooltip(List<String> tooltip) {
        this.tooltip = tooltip;

        return this;
    }

    public JsonMessagePart link(String link) {
        this.link = link;

        return this;
    }

    @Override
    public FancyMessage append(FancyMessage fancyMessage) {
        LegacyConverter.getMessageParts(fancyMessage.getLastColors() + TextUtil.stylish(text)).forEach(messagePart -> {
            fancyMessage.then(messagePart);

            if (command != null)
                fancyMessage.command(command);

            if (suggest != null)
                fancyMessage.suggest(suggest);

            if (link != null)
                fancyMessage.link(link);

            if (tooltip != null
                    && !tooltip.isEmpty())
                fancyMessage.tooltip(tooltip);
        });

        return fancyMessage;
    }
}

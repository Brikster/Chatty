package ru.mrbrikster.chatty;

import lombok.Getter;

public class Chat {

    @Getter private final String name;
    @Getter private final boolean enable;
    @Getter private final String format;
    @Getter private final int range;
    @Getter private final String symbol;

    Chat(String name, boolean enable, String format, int range, String symbol) {
        this.name = name.toLowerCase();
        this.enable = enable;
        this.format = format;
        this.range = range;
        this.symbol = symbol == null ? "" : symbol;
    }

}

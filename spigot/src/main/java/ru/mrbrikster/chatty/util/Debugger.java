package ru.mrbrikster.chatty.util;

import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

public class Debugger {

    private final Chatty chatty;
    private boolean debug;

    public Debugger(Chatty chatty) {
        Configuration configuration =chatty.getExact(Configuration.class);

        this.chatty = chatty;
        this.debug = configuration.getNode("general.debug").getAsBoolean(false);

        configuration.onReload(config -> this.debug = config.getNode("general.debug").getAsBoolean(false));
    }

    @SuppressWarnings("all")
    public void debug(String msg, String... strings) {
        if (debug) chatty.getLogger().info("[DEBUG] " + String.format(msg, strings));
    }

}

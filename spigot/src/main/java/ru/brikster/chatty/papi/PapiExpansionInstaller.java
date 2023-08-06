package ru.brikster.chatty.papi;

import com.google.inject.Injector;

public class PapiExpansionInstaller {

    public static void install(Injector injector) {
        injector.getInstance(ChattyPlaceholderApiExpansion.class).register();
    }
}

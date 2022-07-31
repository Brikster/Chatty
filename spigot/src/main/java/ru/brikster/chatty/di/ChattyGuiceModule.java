package ru.brikster.chatty.di;

import com.google.inject.AbstractModule;
import ru.brikster.chatty.Chatty;
import ru.brikster.chatty.chat.construct.MessageConstructor;
import ru.brikster.chatty.chat.construct.MessageConstructorImpl;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.chat.registry.MemoryChatRegistry;
import ru.brikster.chatty.chat.selection.ChatSelector;
import ru.brikster.chatty.chat.selection.ChatSelectorImpl;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.convert.component.MiniMessageConverter;
import ru.brikster.chatty.convert.message.LegacyToMiniMessageConverter;
import ru.brikster.chatty.convert.message.MessageConverter;
import ru.brikster.chatty.prefix.DefaultPrefixProvider;
import ru.brikster.chatty.prefix.PrefixProvider;
import ru.mrbrikster.baseplugin.config.Configuration;

public class ChattyGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(Chatty.get().getConfiguration());
        bind(ChatRegistry.class).toInstance(new MemoryChatRegistry());

        bind(ComponentConverter.class).to(MiniMessageConverter.class);
        bind(MessageConverter.class).to(LegacyToMiniMessageConverter.class);
        bind(MessageConstructor.class).to(MessageConstructorImpl.class);
        bind(PrefixProvider.class).to(DefaultPrefixProvider.class);
        bind(ChatSelector.class).to(ChatSelectorImpl.class);
    }

}

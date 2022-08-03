package ru.brikster.chatty;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.message.strategy.impl.ConvertComponentMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.RemoveChatSymbolMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.papi.PlaceholderApiMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.vault.PrefixMessageTransformStrategy;
import ru.brikster.chatty.config.object.ChatConfigDeclaration;
import ru.brikster.chatty.guice.ChattyGuiceModule;
import ru.brikster.chatty.misc.MiscellaneousListener;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;

import lombok.SneakyThrows;

import java.util.Map.Entry;
import java.util.logging.Level;

public final class Chatty extends BukkitBasePlugin {

    private static Chatty instance;

    @SneakyThrows
    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        ChattyInitEvent initEvent = new ChattyInitEvent(BukkitAudiences.create(this));
        getServer().getPluginManager().callEvent(initEvent);

        ChattyGuiceModule guiceModule = new ChattyGuiceModule(
                Chatty.this,
                initEvent.getAudienceProvider(),
                getDataFolder().toPath());

        Injector injector = Guice.createInjector(guiceModule);

        injector.injectMembers(ConvertComponentMessageTransformStrategy.instance());
        injector.injectMembers(PrefixMessageTransformStrategy.instance());

        for (Entry<String, ChatConfigDeclaration> entry : guiceModule.getChatsConfig().getChats().entrySet()) {
            ChatConfigDeclaration declaration = entry.getValue();
            Chat chat = new ChatImpl(entry.getKey(),
                    declaration.getDisplayName(), guiceModule.getConverter().convert(declaration.getFormat()),
                    declaration.getSymbol(), null, declaration.getRange(), false);

            chat.addStrategy(RemoveChatSymbolMessageTransformStrategy.instance());
            chat.addStrategy(ConvertComponentMessageTransformStrategy.instance());
            chat.addStrategy(PrefixMessageTransformStrategy.instance());
            chat.addStrategy(PlaceholderApiMessageTransformStrategy.instance());

            guiceModule.getChatRegistry().register(chat);
        }

        EventPriority priority = guiceModule.getSettingsConfig().getListenerPriority();
        if (priority == EventPriority.MONITOR) {
            priority = EventPriority.HIGHEST;
            getLogger().log(Level.WARNING, "Cannot use monitor priority for listener");
        }

        LegacyEventExecutor chatListener = injector.getInstance(LegacyEventExecutor.class);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, chatListener, Chatty.instance, true);

        MiscellaneousListener miscListener = injector.getInstance(MiscellaneousListener.class);
        this.getServer().getPluginManager().registerEvents(miscListener, this);

//        if (config.getNode("general.bungeecord").getAsBoolean(false)) {
//            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
//            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener(getExact(ChatManager.class)));
//        }

//        Chatty.api = new ChattyApiImpl(getExact(ChatManager.class).getChats().stream().filter(Chat::isEnable).collect(Collectors.toSet()));
//        ChattyApiHolder.setApi(api);

//        MetricsUtil.run();
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
//        this.getExact(CommandManager.class).unregisterAll();
//        this.getExact(ChatManager.class).getChats().forEach(chat -> {
//            if (chat.getBukkitCommand() != null) {
//                chat.getBukkitCommand().unregister(Chatty.get());
//            }
//        });
    }

}

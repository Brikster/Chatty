package ru.brikster.chatty;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.brikster.chatty.api.ChattyApi;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.di.ChattyGuiceModule;
import ru.brikster.chatty.misc.MiscellaneousListener;
import ru.brikster.chatty.util.MetricsUtil;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;

import lombok.SneakyThrows;

import java.nio.file.Files;

public final class Chatty extends BukkitBasePlugin {

    private static Chatty instance;
    private static ChattyApi api;

    public static Chatty get() {
        return Chatty.instance;
    }

    /**
     * Returns API object for interacting with Chatty
     *
     * @return API object
     */
    public ChattyApi api() {
        return Chatty.api;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;
        Injector injector = Guice.createInjector(new ChattyGuiceModule());

        Configuration config = getConfiguration();

        if (!config.getNode("config-version")
                .getAsString("0.0")
                .equals("3.0")) {
            Files.move(
                    getDataFolder().toPath().resolve("config.yml"),
                    getDataFolder().toPath().resolve("config.yml.old"));

            config = getConfiguration("config.yml");
        }

        EventPriority eventPriority;
        try {
            String priorityName = config.getNode("general.priority").getAsString("normal").toUpperCase();
            eventPriority = EventPriority.valueOf(priorityName);

            if (eventPriority == EventPriority.MONITOR) {
                eventPriority = EventPriority.NORMAL;
            }
        } catch (IllegalArgumentException e) {
            eventPriority = EventPriority.NORMAL;
        }

        LegacyEventExecutor chatListener = new LegacyEventExecutor();
        injector.injectMembers(chatListener);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, eventPriority, chatListener, Chatty.instance, true);

        MiscellaneousListener miscListener = new MiscellaneousListener();
        injector.injectMembers(miscListener);
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

package ru.brikster.chatty;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.brikster.chatty.api.ChattyApi;
import ru.brikster.chatty.api.chat.Chat;
import ru.brikster.chatty.chat.ChatImpl;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.handle.strategy.impl.SimpleComponentStrategy;
import ru.brikster.chatty.chat.registry.ChatRegistry;
import ru.brikster.chatty.config.Configs;
import ru.brikster.chatty.config.config.ChatsConfig;
import ru.brikster.chatty.config.config.MessagesConfig;
import ru.brikster.chatty.config.config.SettingsConfig;
import ru.brikster.chatty.config.object.ChatConfigDeclaration;
import ru.brikster.chatty.config.serdes.SerdesChatty;
import ru.brikster.chatty.convert.component.ComponentConverter;
import ru.brikster.chatty.di.ChattyGuiceModule;
import ru.brikster.chatty.misc.MiscellaneousListener;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.util.Map.Entry;

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
        SerdesChatty serdesChatty = new SerdesChatty(injector);

        SettingsConfig settingsConfig = ConfigManager.create(SettingsConfig.class, config -> {
            config.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit(), serdesChatty);
            config.withBindFile(getDataFolder().toPath().resolve("settings.yml"));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });

        ChatsConfig chatsConfig = ConfigManager.create(ChatsConfig.class, config -> {
            config.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit(), serdesChatty);
            config.withBindFile(getDataFolder().toPath().resolve("chats.yml"));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });

        Configs.MESSAGES = ConfigManager.create(MessagesConfig.class, config -> {
            config.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit(), serdesChatty);
            config.withBindFile(getDataFolder().toPath().resolve("lang").resolve(settingsConfig.getLanguage() + ".yml"));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });

        ChatRegistry registry = injector.getInstance(ChatRegistry.class);
        ComponentConverter converter = injector.getInstance(ComponentConverter.class);

        for (Entry<String, ChatConfigDeclaration> entry : chatsConfig.getChats().entrySet()) {
            ChatConfigDeclaration declaration = entry.getValue();
            Chat chat = new ChatImpl(entry.getKey(),
                    declaration.getDisplayName(), converter.convert(declaration.getFormat()),
                    declaration.getSymbol(), null, declaration.getRange(), false);

            chat.addStrategy(new SimpleComponentStrategy());

            registry.register(chat);
        }

        ////////////////////

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

package ru.brikster.chatty;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.brikster.chatty.api.event.ChattyInitEvent;
import ru.brikster.chatty.chat.executor.LegacyEventExecutor;
import ru.brikster.chatty.chat.message.strategy.impl.ConvertComponentMessageTransformStrategy;
import ru.brikster.chatty.chat.message.strategy.impl.vault.PrefixMessageTransformStrategy;
import ru.brikster.chatty.config.type.SettingsConfig;
import ru.brikster.chatty.guice.ConfigsLoader;
import ru.brikster.chatty.guice.GeneralGuiceModule;
import ru.brikster.chatty.misc.VanillaListener;

import java.util.logging.Level;

public final class Chatty extends JavaPlugin {

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

    @SneakyThrows
    @Override
    public void onEnable() {
        ChattyInitEvent initEvent = new ChattyInitEvent(BukkitAudiences.create(this));
        getServer().getPluginManager().callEvent(initEvent);

        Injector injector = Guice.createInjector(new GeneralGuiceModule(
                        Chatty.this,
                        initEvent.getAudienceProvider(),
                        getDataFolder().toPath()));

        injector.injectMembers(new ConfigsLoader());

        injector.injectMembers(ConvertComponentMessageTransformStrategy.instance());
        injector.injectMembers(PrefixMessageTransformStrategy.instance());

        EventPriority priority = injector.getInstance(SettingsConfig.class).getListenerPriority();
        if (priority == EventPriority.MONITOR) {
            priority = EventPriority.HIGHEST;
            getLogger().log(Level.WARNING, "Cannot use monitor priority for listener");
        }

        LegacyEventExecutor chatListener = injector.getInstance(LegacyEventExecutor.class);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, chatListener, this, true);

        VanillaListener miscListener = injector.getInstance(VanillaListener.class);
        this.getServer().getPluginManager().registerEvents(miscListener, this);

//        if (config.getNode("general.bungeecord").getAsBoolean(false)) {
//            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
//            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener(getExact(ChatManager.class)));
//        }

//        Chatty.api = new ChattyApiImpl(getExact(ChatManager.class).getChats().stream().filter(Chat::isEnable).collect(Collectors.toSet()));
//        ChattyApiHolder.setApi(api);

//        MetricsUtil.run();
    }

}

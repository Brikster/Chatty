package ru.mrbrikster.chatty;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;
import ru.mrbrikster.chatty.api.ChattyApi;
import ru.mrbrikster.chatty.api.ChattyApi.ChattyApiHolder;
import ru.mrbrikster.chatty.api.ChattyApiImplementation;
import ru.mrbrikster.chatty.bungee.BungeeCordListener;
import ru.mrbrikster.chatty.chat.Chat;
import ru.mrbrikster.chatty.chat.ChatListener;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.commands.CommandManager;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.dependencies.PlayerTagManager;
import ru.mrbrikster.chatty.miscellaneous.MiscellaneousListener;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.notifications.NotificationManager;
import ru.mrbrikster.chatty.util.Debugger;
import ru.mrbrikster.chatty.util.Messages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Chatty extends BukkitBasePlugin {

    private static Chatty instance;
    private static ChattyApi api;
    private final Map<Class<?>, Object> dependenciesMap = new HashMap<>();
    private Configuration configuration;

    public static Chatty instance() {
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

    public Messages messages() {
        return getExact(Messages.class);
    }

    @NotNull
    @SuppressWarnings("all")
    public <T> Optional<T> get(Class<T> clazz) {
        return (Optional<T>) Optional.ofNullable(dependenciesMap.get(clazz));
    }

    @SuppressWarnings("all")
    public <T> T getExact(Class<T> clazz) {
        return (T) dependenciesMap.get(clazz);
    }

    public <T> void register(Class<T> clazz, T object) {
        if (dependenciesMap.containsKey(clazz)) {
            throw new IllegalStateException("Dependency is already registered");
        }

        dependenciesMap.put(clazz, object);
    }

    public <T> void unregister(Class<T> clazz) {
        if (!dependenciesMap.containsKey(clazz)) {
            throw new IllegalStateException("Dependency is not registered");
        }

        dependenciesMap.remove(clazz);
    }


    @Override
    public void onEnable() {
        Chatty.instance = Chatty.this;

        configuration = getConfiguration();

        if (!configuration.getNode("config-version").getAsString("0.0").equals("2.0")) {
            File file = new File(getDataFolder(), "config.yml");
            file.renameTo(new File(getDataFolder(), "config.yml.old"));

            configuration = getConfiguration("config.yml");
        }

        register(Configuration.class, configuration);
        register(ModerationManager.class, new ModerationManager(this));
        register(JsonStorage.class, new JsonStorage(this));

        register(DependencyManager.class, new DependencyManager(this));

        register(PlayerTagManager.class, new PlayerTagManager(this));
        register(ChatManager.class, new ChatManager(this));

        register(Messages.class, new Messages(this));
        register(Debugger.class, new Debugger(this));

        configuration.onReload(config -> {
            unregister(Messages.class);
            register(Messages.class, new Messages(this));
        });

        register(CommandManager.class, new CommandManager(this));
        register(NotificationManager.class, new NotificationManager(this));

        EventPriority eventPriority;
        try {
            String priorityName = configuration.getNode("general.priority").getAsString("normal").toUpperCase();
            eventPriority = EventPriority.valueOf(priorityName);

            if (eventPriority == EventPriority.MONITOR) {
                eventPriority = EventPriority.NORMAL;
            }
        } catch (IllegalArgumentException e) {
            eventPriority = EventPriority.NORMAL;
        }

        ChatListener chatListener = new ChatListener(this);

        this.getServer().getPluginManager().registerEvents(chatListener, this);
        this.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, eventPriority, chatListener, Chatty.instance, true);

        this.getServer().getPluginManager().registerEvents(new MiscellaneousListener(this), this);

        if (configuration.getNode("general.bungeecord").getAsBoolean(false)) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener(getExact(ChatManager.class)));
        }

        Chatty.api = new ChattyApiImplementation(getExact(ChatManager.class).getChats().stream().filter(Chat::isEnable).collect(Collectors.toSet()));
        ChattyApiHolder.setApi(api);

        runMetrics();
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.getExact(CommandManager.class).unregisterAll();
        this.getExact(ChatManager.class).getChats().forEach(chat -> {
            if (chat.getBukkitCommand() != null) {
                chat.getBukkitCommand().unregister(Chatty.instance());
            }
        });
    }

    private void runMetrics() {
        if (configuration.getNode("general.metrics").getAsBoolean(true)) {
            Metrics metrics = new Metrics(this, 3466);
            metrics.addCustomChart(new SimplePie("language",
                    () -> configuration.getNode("general.locale").getAsString("en")));

            metrics.addCustomChart(new SimplePie("json",
                    () -> String.valueOf(configuration.getNode("json.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("private_messaging",
                    () -> String.valueOf(configuration.getNode("pm.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("spy",
                    () -> String.valueOf(configuration.getNode("spy.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("chat_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.chat.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("actionbar_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.actionbar.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("advancements_notifications",
                    () -> String.valueOf(configuration.getNode("notifications.advancements.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("caps_moderation_method",
                    () -> String.valueOf(configuration.getNode("moderation.caps.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("adv_moderation_method",
                    () -> String.valueOf(configuration.getNode("moderation.advertisement.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("swear_moderation_method",
                    () -> String.valueOf(configuration.getNode("moderation.swear.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("miscellaneous_auto_nte",
                    () -> String.valueOf(configuration.getNode("miscellaneous.commands.prefix.auto-nte").getAsBoolean(false)
                            || configuration.getNode("miscellaneous.commands.suffix.auto-nte").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("miscellaneous_join_msg",
                    () -> String.valueOf(configuration.getNode("miscellaneous.vanilla.join.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("miscellaneous_quit_msg",
                    () -> String.valueOf(configuration.getNode("miscellaneous.vanilla.quit.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("miscellaneous_death_msg",
                    () -> String.valueOf(configuration.getNode("miscellaneous.vanilla.death.enable").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("uuid",
                    () -> String.valueOf(configuration.getNode("general.uuid").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("bungeecord",
                    () -> String.valueOf(configuration.getNode("general.bungeecord").getAsBoolean(false))));

            metrics.addCustomChart(new SimplePie("debug",
                    () -> String.valueOf(configuration.getNode("general.debug").getAsBoolean(false))));
        }
    }

}

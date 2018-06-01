package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.chatty.managers.CommandManager;
import ru.mrbrikster.chatty.managers.EventManager;
import ru.mrbrikster.chatty.managers.LogManager;
import ru.mrbrikster.chatty.managers.ViewManager;

public final class Main extends JavaPlugin {

    @Getter private EventManager eventManager;
    @Getter private Config configuration;
    @Getter private ViewManager viewManager;
    @Getter private CommandManager commandManager;
    @Getter private LogManager logManager;

    @Override
    public void onEnable() {
        this.init();

        this.commandManager = new CommandManager(this);

        try {
            this.eventManager = (EventManager) Class.forName("ru.mrbrikster.chatty.listeners." + configuration.getPriority().name()).getConstructor(Main.class).newInstance(this);
        } catch (Exception ex) {
            this.eventManager = new ru.mrbrikster.chatty.listeners.NORMAL(this);
        }

        this.logManager = new LogManager(this);
    }

    public void init() {
        this.configuration = new Config(this);
        this.viewManager = new ViewManager(this);
    }

}

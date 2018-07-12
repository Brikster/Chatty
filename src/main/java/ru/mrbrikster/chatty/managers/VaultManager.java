package ru.mrbrikster.chatty.managers;

import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.mrbrikster.chatty.Main;

public class VaultManager {

    @Getter private final Chat chat;
    @Getter private final Economy economy;

    public VaultManager(Main main) {
        RegisteredServiceProvider<Chat> chatRegisteredServiceProvider = main.getServer().getServicesManager().getRegistration(Chat.class);
        RegisteredServiceProvider<Economy> economyRegisteredServiceProvider = main.getServer().getServicesManager().getRegistration(Economy.class);

        if (chatRegisteredServiceProvider != null)
            this.chat = chatRegisteredServiceProvider.getProvider();
        else this.chat = null;

        if (economyRegisteredServiceProvider != null)
            this.economy = economyRegisteredServiceProvider.getProvider();
        else this.economy = null;
    }

}

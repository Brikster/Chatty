package ru.mrbrikster.chatty.managers;

import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import ru.mrbrikster.chatty.Main;

class VaultManager {

    @Getter private final Chat chat;

    VaultManager(Main main) {
        this.chat = main.getServer().getServicesManager().getRegistration(Chat.class).getProvider();
    }

}

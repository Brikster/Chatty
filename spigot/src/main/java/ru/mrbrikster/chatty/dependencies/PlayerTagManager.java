package ru.mrbrikster.chatty.dependencies;

import com.google.gson.JsonElement;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.util.Optional;

public class PlayerTagManager {

    private final DependencyManager dependencyManager;
    private final JsonStorage jsonStorage;

    public PlayerTagManager(Chatty chatty) {
        this.dependencyManager = chatty.getExact(DependencyManager.class);
        this.jsonStorage = chatty.getExact(JsonStorage.class);
    }

    public String getPrefix(Player player) {
        String prefix = "";

        Optional<JsonElement> jsonElement = jsonStorage.getProperty(player, "prefix");

        if (jsonElement.isPresent()) {
            return jsonElement.get().getAsString();
        }

        if (dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();
            prefix = vaultHook.getPrefix(player);

            if (prefix == null) prefix = "";
        }

        return prefix;
    }

    public String getSuffix(Player player) {
        String suffix = "";

        Optional<JsonElement> jsonElement = jsonStorage.getProperty(player, "suffix");

        if (jsonElement.isPresent()) {
            return jsonElement.get().getAsString();
        }

        if (dependencyManager.getVault() != null) {
            VaultHook vaultHook = dependencyManager.getVault();
            suffix = vaultHook.getSuffix(player);

            if (suffix == null) suffix = "";
        }

        return suffix;
    }

}

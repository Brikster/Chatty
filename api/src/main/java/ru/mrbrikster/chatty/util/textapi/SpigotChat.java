package ru.mrbrikster.chatty.util.textapi;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Sends JSON messages and titles through the Spigot API instead of NMS.
 * NMS reflection cannot work on Paper 1.20.5+ (the CraftBukkit package is
 * no longer versioned) nor on 26.x (no obfuscated names left), so this path
 * is preferred wherever the API exists. Only ancient servers fall back to NMS.
 */
@UtilityClass
class SpigotChat {

    private static final boolean CHAT_SUPPORTED;
    private static final boolean SENDER_UUID_SUPPORTED;
    private static final boolean TITLE_SUPPORTED;

    static {
        boolean chat = false;
        boolean senderUuid = false;
        boolean title = false;
        try {
            Player.Spigot.class.getMethod("sendMessage", ChatMessageType.class, BaseComponent[].class);
            ComponentSerializer.class.getMethod("parse", String.class);
            chat = true;
            Player.Spigot.class.getMethod("sendMessage", ChatMessageType.class, UUID.class, BaseComponent[].class);
            senderUuid = true;
        } catch (Throwable ignored) {
        }
        try {
            Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            title = chat;
        } catch (Throwable ignored) {
        }
        CHAT_SUPPORTED = chat;
        SENDER_UUID_SUPPORTED = senderUuid;
        TITLE_SUPPORTED = title;
    }

    boolean isChatSupported() {
        return CHAT_SUPPORTED;
    }

    boolean isTitleSupported() {
        return TITLE_SUPPORTED;
    }

    /**
     * @param type "CHAT" or "GAME_INFO" (action bar), as used by {@link NMSUtil}
     */
    void sendChat(Player player, String type, String json, @Nullable Player sender) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        if (type.equals("GAME_INFO")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
        } else if (sender != null && SENDER_UUID_SUPPORTED) {
            player.spigot().sendMessage(ChatMessageType.CHAT, sender.getUniqueId(), components);
        } else {
            player.spigot().sendMessage(ChatMessageType.CHAT, components);
        }
    }

    void sendTitle(Player player, @Nullable String titleJson, @Nullable String subtitleJson,
                   int fadeIn, int stay, int fadeOut) {
        player.sendTitle(toLegacyText(titleJson), toLegacyText(subtitleJson), fadeIn, stay, fadeOut);
    }

    private String toLegacyText(@Nullable String json) {
        return json == null ? "" : TextComponent.toLegacyText(ComponentSerializer.parse(json));
    }

}

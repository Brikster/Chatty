package ru.mrbrikster.chatty.util.textapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

@UtilityClass
public class NMSUtil {

    private static final HashMap<String, Class<?>> NMS_CLASSES = new HashMap<>();

    static {
        NMS_CLASSES.put("IChatBaseComponent", resolveSuitableClass(ServerPackage.MINECRAFT + ".IChatBaseComponent",
                ServerPackage.NETWORK + ".chat.IChatBaseComponent"));
        NMS_CLASSES.put("ChatMessageType", resolveSuitableClass(ServerPackage.MINECRAFT + ".ChatMessageType",
                ServerPackage.NETWORK + ".chat.ChatMessageType"));
        NMS_CLASSES.put("IChatBaseComponent$ChatSerializer", resolveSuitableClass(ServerPackage.MINECRAFT + ".IChatBaseComponent$ChatSerializer",
                ServerPackage.NETWORK + ".chat.IChatBaseComponent$ChatSerializer"));

        NMS_CLASSES.put("PacketPlayOutChat", resolveSuitableClass(ServerPackage.MINECRAFT + ".PacketPlayOutChat",
                ServerPackage.NETWORK + ".protocol.game.PacketPlayOutChat"));
        NMS_CLASSES.put("Packet", resolveSuitableClass(ServerPackage.MINECRAFT + ".Packet",
                ServerPackage.NETWORK + ".protocol.Packet"));

        // Legacy title packets
        NMS_CLASSES.put("PacketPlayOutTitle", resolveSuitableClass(ServerPackage.MINECRAFT + ".PacketPlayOutTitle"));
        NMS_CLASSES.put("PacketPlayOutTitle$EnumTitleAction", resolveSuitableClass(ServerPackage.MINECRAFT + ".PacketPlayOutTitle$EnumTitleAction"));

        // New (>= 1.17) title packets
        NMS_CLASSES.put("ClientboundSetTitleTextPacket", resolveSuitableClass(ServerPackage.NETWORK + ".protocol.game.ClientboundSetTitleTextPacket"));
        NMS_CLASSES.put("ClientboundSetSubtitleTextPacket", resolveSuitableClass(ServerPackage.NETWORK + ".protocol.game.ClientboundSetSubtitleTextPacket"));
        NMS_CLASSES.put("ClientboundSetTitlesAnimationPacket", resolveSuitableClass(ServerPackage.NETWORK + ".protocol.game.ClientboundSetTitlesAnimationPacket"));
    }

    public Class<?> getClass(String key) {
        return NMS_CLASSES.get(key);
    }

    private Class<?> resolveSuitableClass(String... paths) {
        for (String path : paths) {
            try {
                return Class.forName(path);
            } catch (ClassNotFoundException ignored) {}
        }

        return null;
    }

    @NotNull
    public Field resolveField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getField(name);
            } catch (NoSuchFieldException ignored) {}
        }

        throw new IllegalStateException();
    }

    public void sendChatPacket(Player player, String type, String text) {
        try {
            Class<?> clsIChatBaseComponent = NMS_CLASSES.get("IChatBaseComponent");
            Class<?> clsChatMessageType = NMS_CLASSES.get("ChatMessageType");
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = resolveField(entityPlayer.getClass(), "b", "playerConnection").get(entityPlayer);
            Object chatBaseComponent = NMS_CLASSES.get("IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, text);
            Object chatMessageType = clsChatMessageType.getMethod("valueOf", String.class).invoke(null, type);

            Object packetPlayOutChat = null;
            Class<?> packetPlayOutChatClass = NMS_CLASSES.get("PacketPlayOutChat");

            // Legacy versions (< 1.16)
            try {
                packetPlayOutChat = packetPlayOutChatClass.getConstructor(clsIChatBaseComponent, clsChatMessageType)
                        .newInstance(chatBaseComponent, chatMessageType);
            } catch (Throwable ignored) {}

            // New versions (>= 1.16)
            if (packetPlayOutChat == null) {
                try {
                    packetPlayOutChat = packetPlayOutChatClass.getConstructor(clsIChatBaseComponent, clsChatMessageType, UUID.class)
                            .newInstance(chatBaseComponent, chatMessageType, null);
                } catch (Throwable ignored) {}
            }

            if (packetPlayOutChat == null) {
                throw new IllegalStateException();
            }

            playerConnection.getClass().getMethod("sendPacket", NMS_CLASSES.get("Packet"))
                    .invoke(playerConnection, packetPlayOutChat);
        } catch (Throwable e) {
            throw new RuntimeException("NMS features is not supported by Chatty on your server version (" + ServerPackage.getServerVersion() + ")", e);
        }
    }

    public enum ServerPackage {

        MINECRAFT("net.minecraft.server." + getServerVersion()),
        NETWORK("net.minecraft.network");

        private final String path;

        ServerPackage(String path) {
            this.path = path;
        }

        public static String getServerVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().substring(23);
        }

        @Override
        public String toString() {
            return path;
        }

    }

}

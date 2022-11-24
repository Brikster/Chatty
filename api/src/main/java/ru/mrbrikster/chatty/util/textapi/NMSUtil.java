package ru.mrbrikster.chatty.util.textapi;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

import static ru.mrbrikster.chatty.util.textapi.NMSUtil.ServerPackage.*;

@UtilityClass
public class NMSUtil {

    private static final HashMap<String, Class<?>> NMS_CLASSES = new HashMap<>();

    static {
        NMS_CLASSES.put("IChatBaseComponent", resolveSuitableClass(MINECRAFT + ".IChatBaseComponent",
                NETWORK + ".chat.IChatBaseComponent", NETWORK + ".chat.IChatBaseComponent", "net.minecraft.util.IChatComponent"));
        NMS_CLASSES.put("ChatMessageType", resolveSuitableClass(MINECRAFT + ".ChatMessageType",
                NETWORK + ".chat.ChatMessageType"));
        NMS_CLASSES.put("IChatBaseComponent$ChatSerializer", resolveSuitableClass(MINECRAFT + ".IChatBaseComponent$ChatSerializer",
                NETWORK + ".chat.IChatBaseComponent$ChatSerializer", "net.minecraft.util.IChatComponent$Serializer"));

        NMS_CLASSES.put("PacketPlayOutChat", resolveSuitableClass(MINECRAFT + ".PacketPlayOutChat",
                NETWORK + ".protocol.game.PacketPlayOutChat", NETWORK + ".play.server.S02PacketChat"));
        NMS_CLASSES.put("Packet", resolveSuitableClass(MINECRAFT + ".Packet",
                NETWORK + ".protocol.Packet", NETWORK + ".Packet"));

        // Legacy title packets
        NMS_CLASSES.put("PacketPlayOutTitle", resolveSuitableClass(MINECRAFT + ".PacketPlayOutTitle"));
        NMS_CLASSES.put("PacketPlayOutTitle$EnumTitleAction", resolveSuitableClass(MINECRAFT + ".PacketPlayOutTitle$EnumTitleAction"));

        // New (>= 1.17) title packets
        NMS_CLASSES.put("ClientboundSetTitleTextPacket", resolveSuitableClass(NETWORK + ".protocol.game.ClientboundSetTitleTextPacket"));
        NMS_CLASSES.put("ClientboundSetSubtitleTextPacket", resolveSuitableClass(NETWORK + ".protocol.game.ClientboundSetSubtitleTextPacket"));
        NMS_CLASSES.put("ClientboundSetTitlesAnimationPacket", resolveSuitableClass(NETWORK + ".protocol.game.ClientboundSetTitlesAnimationPacket"));

        // 1.19 chat packet
        NMS_CLASSES.put("ClientboundPlayerChatPacket", resolveSuitableClass(NETWORK + ".protocol.game.ClientboundPlayerChatPacket"));
        NMS_CLASSES.put("IChatMutableComponent", resolveSuitableClass(NETWORK + ".protocol.game.IChatMutableComponent"));
        NMS_CLASSES.put("PlayerChatMessage", resolveSuitableClass(NETWORK + ".chat.PlayerChatMessage"));
        NMS_CLASSES.put("ServerPlayer", resolveSuitableClass("net.minecraft.server.level.ServerPlayer"));
        NMS_CLASSES.put("ChatSender", resolveSuitableClass(NETWORK + ".chat.ChatSender"));

        // 1.19.1
        NMS_CLASSES.put("ClientboundSystemChatPacket", resolveSuitableClass(NETWORK + ".protocol.game.ClientboundSystemChatPacket"));
    }

    public Class<?> getClass(String key) {
        return NMS_CLASSES.get(key);
    }

    private Class<?> resolveSuitableClass(String... paths) {
        for (String path : paths) {
            try {
                return Class.forName(path);
            } catch (ClassNotFoundException | NullPointerException ignored) {
            }
        }

        return null;
    }

    @NotNull
    public Field resolveField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }

        throw new IllegalStateException();
    }

    public void sendChatPacket(Player player, String type, String text, @Nullable Player sender) {
        try {
            Class<?> clsIChatBaseComponent = NMS_CLASSES.get("IChatBaseComponent");

            Object chatBaseComponent;
            try {
                chatBaseComponent = NMS_CLASSES.get("IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, text);
            } catch (Throwable ignored) {
                try {
                    // 1.7.10 (Crucible)
                    chatBaseComponent = NMS_CLASSES.get("IChatBaseComponent$ChatSerializer").getMethod("func_150699_a", String.class).invoke(null, text);
                } catch (Throwable ignored1) {
                    chatBaseComponent = null;
                }
            }

            // EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = resolveField(entityPlayer.getClass(), "b", "playerConnection").get(entityPlayer);

            Class<?> clsClientboundPlayerChatPacket = NMS_CLASSES.get("ClientboundPlayerChatPacket");

            if (clsClientboundPlayerChatPacket == null) {
                // < 1.19
                Class<?> clsChatMessageType = NMS_CLASSES.get("ChatMessageType");
                Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object playerConnection = resolveField(entityPlayer.getClass(), "b", "playerConnection", "field_71135_a").get(entityPlayer);
                Object chatMessageType;

                try {
                    chatMessageType = clsChatMessageType.getMethod("valueOf", String.class).invoke(null, type);
                } catch (Throwable ignored) {
                    chatMessageType = null;
                }

                Object packetPlayOutChat = null;
                Class<?> packetPlayOutChatClass = NMS_CLASSES.get("PacketPlayOutChat");

                // Legacy versions (< 1.12)
                if (chatMessageType == null) {
                    try {
                        packetPlayOutChat = packetPlayOutChatClass.getConstructor(clsIChatBaseComponent).newInstance(chatBaseComponent);
                    } catch (Throwable ignored) {
                    }
                }

                // Legacy versions (< 1.16)
                if (packetPlayOutChat == null) {
                    try {
                        packetPlayOutChat = packetPlayOutChatClass.getConstructor(clsIChatBaseComponent, clsChatMessageType)
                                .newInstance(chatBaseComponent, chatMessageType);
                    } catch (Throwable ignored) {
                    }
                }

                // New versions (>= 1.16)
                if (packetPlayOutChat == null) {
                    try {
                        packetPlayOutChat = packetPlayOutChatClass.getConstructor(clsIChatBaseComponent, clsChatMessageType, UUID.class)
                                .newInstance(chatBaseComponent, chatMessageType, sender.getUniqueId());
                    } catch (Throwable ignored) {
                    }
                }

                if (packetPlayOutChat == null) {
                    throw new IllegalStateException();
                }

                Method sendPacketMethod;
                try {
                    sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", NMS_CLASSES.get("Packet"));
                } catch (Exception ignored) {
                    try {
                        // 1.18+
                        sendPacketMethod = playerConnection.getClass().getMethod("a", NMS_CLASSES.get("Packet"));
                    } catch (Exception ignored1) {
                        // 1.7.10 (Crucible)
                        sendPacketMethod = playerConnection.getClass().getMethod("func_147359_a", NMS_CLASSES.get("Packet"));
                    }
                }

                sendPacketMethod.invoke(playerConnection, packetPlayOutChat);
            } else {
                // 1.19+
                Class<?> clsChatSender = NMS_CLASSES.get("ChatSender");
                Class<?> clsPlayerChatMessage = NMS_CLASSES.get("PlayerChatMessage");

                // ChatMessageType chatMessageType = type.equals("CHAT") ? ChatMessageType.b : ChatMessageType.d;
                Object chatMessageType = NMS_CLASSES.get("ChatMessageType")
                        // b: 'system', d: 'game_info'
                        .getDeclaredField(type.equals("CHAT") ? "c" : "d")
                        .get(null);

                Object senderName = NMS_CLASSES.get("IChatBaseComponent$ChatSerializer")
                        .getMethod("a", String.class)
                        .invoke(null, "{\"text\":\"" + player.getDisplayName() + "\"}");

                try {
                    // 1.19 - player message with UUID etc.

                    // PlayerChatMessage playerChatMessage = PlayerChatMessage.a(chatBaseComponent);
                    Object playerChatMessage = clsPlayerChatMessage.getMethod("a", clsIChatBaseComponent)
                            .invoke(null, chatBaseComponent);

                    if (sender == null) {
                        // entityPlayer.a(chatBaseComponent, chatMessageType);
                        entityPlayer.getClass().getMethod("a", clsIChatBaseComponent, chatMessageType.getClass())
                                .invoke(entityPlayer, chatBaseComponent, chatMessageType);
                    } else {
                        // ChatSender chatSender = new ChatSender(sender, senderName);
                        Object chatSender = clsChatSender.getConstructor(UUID.class, clsIChatBaseComponent)
                                .newInstance(sender.getUniqueId(), senderName);
                        // entityPlayer.a(playerChatMessage, chatSender, chatMessageType);
                        entityPlayer.getClass().getMethod("a", clsPlayerChatMessage, clsChatSender, chatMessageType.getClass())
                                .invoke(entityPlayer, playerChatMessage, chatSender, chatMessageType);
                    }
                } catch (Exception ignored) {
                    // 1.19.1 - system message
                    Class<?> clsClientboundSystemChatPacket = NMS_CLASSES.get("ClientboundSystemChatPacket");
                    // ClientboundSystemChatPacket clientboundSystemChatPacket = new ClientboundSystemChatPacket(chatBaseComponent, false);
                    Object clientboundSystemChatPacket = clsClientboundSystemChatPacket.getConstructor(clsIChatBaseComponent, boolean.class)
                            .newInstance(chatBaseComponent, false);

                    // ((CraftPlayer) player).b.a(clientboundSystemChatPacket);
                    playerConnection.getClass().getMethod("a", NMS_CLASSES.get("Packet"))
                            .invoke(playerConnection, clientboundSystemChatPacket);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("NMS features is not supported by Chatty on your server version (" + getServerVersion() + ")", e);
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

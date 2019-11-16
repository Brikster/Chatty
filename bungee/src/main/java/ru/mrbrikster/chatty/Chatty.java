package ru.mrbrikster.chatty;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

public final class Chatty extends Plugin implements Listener {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onServerMessage(PluginMessageEvent e) throws IOException {
        if (e.getTag().equals("BungeeCord")) {
            Server server = (Server) e.getSender();
            ServerInfo serverInfo = server.getInfo();

            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(e.getData()));
            String channel = inputStream.readUTF();

            if (channel.equals("chatty")) {
                String chat = inputStream.readUTF();
                String text = inputStream.readUTF();
                boolean json = inputStream.readBoolean();

                ByteArrayOutputStream stream;
                DataOutputStream outputStream = new DataOutputStream(stream = new ByteArrayOutputStream());

                outputStream.writeUTF(channel);
                outputStream.writeUTF(chat);
                outputStream.writeUTF(text);
                outputStream.writeBoolean(json);

                ProxyServer.getInstance().getServers().forEach((name, info) -> {
                    if (!serverInfo.getName().equals(name)) {
                        info.sendData("BungeeCord", stream.toByteArray(), false);
                    }
                });
            }
        }
    }

}

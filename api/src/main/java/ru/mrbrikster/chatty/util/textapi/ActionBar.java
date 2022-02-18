/*
 This file is part of TextAPI 2.0.
 Copyright (c) 2015 Luca P. <https://github.com/TheLuca98>

 TextAPI is free software: you can redistribute it and/or modify it under the
 terms of the GNU Lesser General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any
 later version.

 TextAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.

 You should have received a copy of the GNU Lesser General Public License along
 with TextAPI. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.mrbrikster.chatty.util.textapi;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Represents a message displayed above the hotbar.
 *
 * @author Luca
 */
public class ActionBar {

    private JsonObject json;

    /**
     * Constructs an {@link ActionBar} object based on plain text.
     *
     * @param text Text to display.
     */
    public ActionBar(String text) {
        Preconditions.checkNotNull(text);
        this.json = Title.convert(text);
    }

    /**
     * Constructs an {@link ActionBar} object based on JSON-formatted text.
     *
     * @param json Text to display Must be in /tellraw JSON format.
     */
    public ActionBar(JsonObject json) {
        Preconditions.checkNotNull(json);
        this.json = json;
    }

    /**
     * This method has been kept just to ensure backwards compatibility with older versions of TextAPI.
     * It is not supported and will be removed in a future release.
     *
     * @param player  The player to send the message to.
     * @param message The message to send.
     * @deprecated Please create a new {@link ActionBar} instance instead.
     */
    @Deprecated
    public static void send(Player player, String message) {
        new ActionBar(message).send(player);
    }

    /**
     * This method has been kept just to ensure backwards compatibility with older versions of TextAPI.
     * It is not supported and will be removed in a future release.
     *
     * @param message The message to send.
     * @deprecated Please create a new {@link ActionBar} instance instead.
     */
    @Deprecated
    public static void sendToAll(String message) {
        new ActionBar(message).sendToAll();
    }

    /**
     * Sends an action bar message to a specific player.
     *
     * @param player The player to send the message to.
     */
    public void send(Player player) {
        NMSUtil.sendChatPacket(player, "GAME_INFO", json.toString(), null);
    }

    /**
     * Sends an action bar message to all online players.
     */
    public void sendToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player);
        }
    }

    /**
     * Changes the text to display.
     *
     * @param text Text to display.
     */
    public void setText(String text) {
        Preconditions.checkNotNull(text);
        this.json = Title.convert(text);
    }

    /**
     * Changes the text to display.
     *
     * @param json Text to display. Must be in /tellraw JSON format.
     */
    public void setJsonText(JsonObject json) {
        Preconditions.checkNotNull(json);
        this.json = json;
    }

}

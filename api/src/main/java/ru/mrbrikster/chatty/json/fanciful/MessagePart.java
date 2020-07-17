package ru.mrbrikster.chatty.json.fanciful;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.stream.JsonWriter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import ru.mrbrikster.chatty.util.TextUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.MAGIC;

/**
 * Internal class: Represents a component of a JSON-serializable {@link FancyMessage}.
 */
@SuppressWarnings("all")
public final class MessagePart implements JsonRepresentedObject, ConfigurationSerializable, Cloneable {

    ChatColor color = ChatColor.WHITE;
    ArrayList<ChatColor> styles = new ArrayList<ChatColor>();
    String clickActionName = null, clickActionData = null, hoverActionName = null;
    JsonRepresentedObject hoverActionData = null;
    public TextualComponent text = null;
    String insertionData = null;
    ArrayList<JsonRepresentedObject> translationReplacements = new ArrayList<JsonRepresentedObject>();

    MessagePart(final TextualComponent text) {
        this.text = text;
    }

    MessagePart() {
        this.text = null;
    }

    boolean hasText() {
        return text != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MessagePart clone() throws CloneNotSupportedException {
        MessagePart obj = (MessagePart) super.clone();
        obj.styles = (ArrayList<ChatColor>) styles.clone();
        if (hoverActionData instanceof JsonString) {
            obj.hoverActionData = new JsonString(((JsonString) hoverActionData).getValue());
        } else if (hoverActionData instanceof FancyMessage) {
            obj.hoverActionData = ((FancyMessage) hoverActionData).clone();
        }
        obj.translationReplacements = (ArrayList<JsonRepresentedObject>) translationReplacements.clone();
        return obj;

    }

    static final BiMap<ChatColor, String> stylesToNames;

    static {
        ImmutableBiMap.Builder<ChatColor, String> builder = ImmutableBiMap.builder();
        for (final ChatColor style : ChatColor.values()) {
            if (TextUtil.isColor(style)) {
                continue;
            }

            String styleName;
            if (MAGIC.equals(style)) {
                styleName = "obfuscated";
            } else if (ChatColor.UNDERLINE.equals(style)) {
                styleName = "underlined";
            } else {
                styleName = style.name().toLowerCase();
            }

            builder.put(style, styleName);
        }
        stylesToNames = builder.build();
    }

    public void writeJson(JsonWriter json) {
        try {
            json.beginObject();
            text.writeJson(json);
            json.name("color").value(color.getName());
            for (final ChatColor style : styles) {
                json.name(stylesToNames.get(style)).value(true);
            }
            if (clickActionName != null && clickActionData != null) {
                json.name("clickEvent")
                        .beginObject()
                        .name("action").value(clickActionName)
                        .name("value").value(clickActionData)
                        .endObject();
            }
            if (hoverActionName != null && hoverActionData != null) {
                json.name("hoverEvent")
                        .beginObject()
                        .name("action").value(hoverActionName)
                        .name("value");
                hoverActionData.writeJson(json);
                json.endObject();
            }
            if (insertionData != null) {
                json.name("insertion").value(insertionData);
            }
            if (translationReplacements.size() > 0 && text != null && TextualComponent.isTranslatableText(text)) {
                json.name("with").beginArray();
                for (JsonRepresentedObject obj : translationReplacements) {
                    obj.writeJson(json);
                }
                json.endArray();
            }
            json.endObject();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "A problem occured during writing of JSON string", e);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("text", text);
        map.put("styles", styles);
        map.put("color", color.getName());
        map.put("hoverActionName", hoverActionName);
        map.put("hoverActionData", hoverActionData);
        map.put("clickActionName", clickActionName);
        map.put("clickActionData", clickActionData);
        map.put("insertion", insertionData);
        map.put("translationReplacements", translationReplacements);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static MessagePart deserialize(Map<String, Object> serialized) {
        MessagePart part = new MessagePart((TextualComponent) serialized.get("text"));
        part.styles = (ArrayList<ChatColor>) serialized.get("styles");
        part.color = ChatColor.of(serialized.get("color").toString());
        part.hoverActionName = (String) serialized.get("hoverActionName");
        part.hoverActionData = (JsonRepresentedObject) serialized.get("hoverActionData");
        part.clickActionName = (String) serialized.get("clickActionName");
        part.clickActionData = (String) serialized.get("clickActionData");
        part.insertionData = (String) serialized.get("insertion");
        part.translationReplacements = (ArrayList<JsonRepresentedObject>) serialized.get("translationReplacements");
        return part;
    }

    static {
        ConfigurationSerialization.registerClass(MessagePart.class);
    }

}

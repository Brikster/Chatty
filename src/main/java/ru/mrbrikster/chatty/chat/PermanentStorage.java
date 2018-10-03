package ru.mrbrikster.chatty.chat;

import com.google.gson.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Optional;

public class PermanentStorage {

    private final File storageFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PermanentStorage(JavaPlugin javaPlugin) {
        this.storageFile = new File(javaPlugin.getDataFolder(), "storage.json");

        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setProperty(String player, String property, JsonElement value) {
        JsonElement jsonObject = null;
        try {
            jsonObject = new JsonParser().parse(read());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonObject == null || !jsonObject.isJsonObject())
            jsonObject = new JsonObject();
        else jsonObject = jsonObject.getAsJsonObject();

        JsonElement propertyElement;
        if (((JsonObject) jsonObject).has(property)) {
            propertyElement = ((JsonObject) jsonObject).remove(property);

            if (propertyElement.isJsonObject())
                propertyElement = propertyElement.getAsJsonObject();
            else propertyElement = new JsonObject();
        } else propertyElement = new JsonObject();

        if (((JsonObject) propertyElement).has(player))
            ((JsonObject) propertyElement).remove(player);

        ((JsonObject) propertyElement).add(player, value);

        ((JsonObject) jsonObject).add(property, propertyElement);

        try {
            write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setProperty(Player player, String property, JsonElement value) {
        setProperty(player.getName(), property, value);
    }

    public Optional<JsonElement> getProperty(String player, String property) {
        try {
            JsonElement jsonObject = new JsonParser().parse(read());

            if (!jsonObject.isJsonObject())
                return Optional.empty();

            JsonElement propertyElement = ((JsonObject) jsonObject).get(property);

            if (propertyElement.isJsonObject()) {
                JsonElement playerPropertyElement = propertyElement.getAsJsonObject().get(player);

                return Optional.ofNullable(playerPropertyElement);
            }

            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<JsonElement> getProperty(Player player, String property) {
        return getProperty(player.getName(), property);
    }

    private String read() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(storageFile));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        reader.close();

        return stringBuilder.toString();
    }

    private void write(String json) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile));

        writer.write(json);
        writer.flush();
        writer.close();
    }

}

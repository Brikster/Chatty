package ru.mrbrikster.chatty.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class JsonUtil {

    public static boolean contains(JsonArray jsonArray, JsonElement jsonElement) {
        if (jsonElement == null)
            return false;

        for (JsonElement element : jsonArray) {
            if (jsonElement.equals(element))
                return true;
        }

        return false;
    }

}

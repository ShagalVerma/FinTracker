package com.financetracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static Map<String, Object> parseBody(String json) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return GSON.fromJson(json, type);
    }

    /** Builds a simple {"error": "msg"} JSON string. */
    public static String error(String message) {
        return "{\"error\": \"" + message.replace("\"", "'") + "\"}";
    }

    /** Builds a simple {"message": "msg"} JSON string. */
    public static String message(String msg) {
        return "{\"message\": \"" + msg.replace("\"", "'") + "\"}";
    }
}

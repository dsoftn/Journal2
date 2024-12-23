package com.dsoftn.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import java.util.Map;


public class UJson {

    public static String mapToJsonString(Map<String, Object> map) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

        return gson.toJson(map);
    }

    public static boolean saveJsonFile(String filePath, Map<String, Object> map) {
        String jsonString = mapToJsonString(map);
        try {
            Files.write(Path.of(filePath), jsonString.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            UError.exception("Failed to save Json to file: " + filePath, e);
            return false;
        }
    }

    public static PyDict jsonToPyDict(String json) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

        return gson.fromJson(json, new TypeToken<PyDict>() {}.getType());
    }

    public static Map<String, Object> jsonToMap(String json) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

        return gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
    }

    public static String loadJsonFileToString(String filePath) {
        try {
            return new String(Files.readAllBytes(Path.of(filePath)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            UError.exception("Failed to load Json from file: " + filePath, e);
            return null;
        }
    }

    public static Map<String, Object> loadJsonFileToMap(String filePath) {
        return jsonToMap(loadJsonFileToString(filePath));
    }

    public static PyDict loadJsonFileToPyDict(String filePath) {
        return jsonToPyDict(loadJsonFileToString(filePath));
    }


}

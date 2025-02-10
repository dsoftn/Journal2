package com.dsoftn.utils;

public class UJavaFX {

    public static String getUniqueId() {
        return System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString();
    }
}

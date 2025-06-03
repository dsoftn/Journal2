package com.dsoftn.utils;

import com.dsoftn.OBJECTS;
import com.dsoftn.Settings.SettingsItem;
import com.dsoftn.services.text_handler.TextHandler;

public class USettings {

    public static SettingsItem getAppOrUserSettingsItem(String key, String settingsName, String defaultStringValue, Integer defaultIntValue) {
        String suffix = "";
        if (settingsName != null) {
            suffix = "_" + settingsName;
        } else {
            return OBJECTS.SETTINGS.getUserSettingsItem(key);
        }

        if (!OBJECTS.SETTINGS.isAppSettingExists(key + suffix)) {
            SettingsItem newItem = OBJECTS.SETTINGS.getUserSettingsItem(key).duplicate();
            newItem.setKey(key + suffix);
            if (defaultStringValue != null) {
                newItem.setValue(defaultStringValue);
                newItem.setDefaultValue(defaultStringValue);
            }
            if (defaultIntValue != null) {
                newItem.setValue(defaultIntValue);
                newItem.setDefaultValue(defaultIntValue);
            }
            newItem.setCanBeSavedInFile(true);
            OBJECTS.SETTINGS.addAppSettings(newItem);
        }
        
        return OBJECTS.SETTINGS.getAppSettingsItem(key + suffix);
    }

    public static SettingsItem getAppOrUserSettingsItem(String key, TextHandler.Behavior behavior, String defaultStringValue, Integer defaultIntValue) {
        return getAppOrUserSettingsItem(key, behavior.name(), defaultStringValue, defaultIntValue);
    }

    public static SettingsItem getAppOrUserSettingsItem(String key, TextHandler.Behavior behavior) {
        return getAppOrUserSettingsItem(key, behavior, null, null);
    }

    public static SettingsItem getAppOrUserSettingsItem(String key, TextHandler.Behavior behavior, String defaultStringValue) {
        return getAppOrUserSettingsItem(key, behavior, defaultStringValue, null);
    }

    public static SettingsItem getAppOrUserSettingsItem(String key, TextHandler.Behavior behavior, Integer defaultIntValue) {
        return getAppOrUserSettingsItem(key, behavior, null, defaultIntValue);
    }



}

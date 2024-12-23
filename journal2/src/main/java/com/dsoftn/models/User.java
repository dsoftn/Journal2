package com.dsoftn.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.dsoftn.utils.LanguagesEnum;
import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;

public class User {
    // Constants
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private DateTimeFormatter dateTimeFormatterForJson = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    // Properties
    // Mandatory
    private String username = "";
    private LanguagesEnum language = LanguagesEnum.UNKNOWN;
    private String password = "";
    // Paths
    private String pathName = ""; // Name of folder where user data is stored
    private String userInfoPath = ""; // Name of user info file
    private String dbPath = ""; // Name of database file
    private String userSettingsPath = ""; // Name of user settings file
    private String appSettingsPath = ""; // Name of application settings file
    private String attachmentsFolderPath = "Attachments"; // Name of folder where user attachments are stored
    // Other details
    private String name = "";
    private String email = "";
    private String phone = "";
    private String address = "";
    private String notes = "";
    // Timestamps
    private LocalDateTime created = LocalDateTime.now();
    private int loginSessions = 0; // Number of login sessions
    private int loginDurationSeconds = 0; // Total login duration in seconds
    // Flags
    private boolean isLoggedIn = false;
    private boolean multipleLoginAllowed = OBJECTS.SETTINGS.getvBOOLEAN("AllowMultipleLogins");


    // Public methods


    // Getters
    public String getUsername() { return username; }
    public LanguagesEnum getLanguage() { return language; }
    public String getPassword() { return password; }
    public String getPathName() { return pathName; }
    public String getUserInfoPath() { return userInfoPath; }
    public String getDbPath() { return dbPath; }
    public String getUserSettingsPath() { return userSettingsPath; }
    public String getAppSettingsPath() { return appSettingsPath; }
    public String getAttachmentsFolderPath() { return attachmentsFolderPath; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNotes() { return notes; }
    public String getCreated() { return created.format(dateTimeFormatter); }
    public String getCreatedForJson() { return created.format(dateTimeFormatterForJson); }
    public int getLoginSessions() { return loginSessions; }
    public int getLoginDurationSeconds() { return loginDurationSeconds; }
    public boolean getIsLoggedIn() { return isLoggedIn; }
    public boolean getMultipleLoginAllowed() { return multipleLoginAllowed; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setLanguage(LanguagesEnum language) { this.language = language; }
    public void setPassword(String password) { this.password = password; }

    public void setPathName(String pathName) {
        if (pathName == null || pathName.isEmpty()) {
            this.pathName = "";
            this.userInfoPath = "";
            this.dbPath = "";
            this.userSettingsPath = "";
            this.appSettingsPath = "";
            this.attachmentsFolderPath = "";
            return;
        }

        this.pathName = pathName;
        this.userInfoPath = CONSTANTS.FOLDER_DATA_USERS + "/" + this.pathName + "/" + this.pathName + "_info.json";
        this.dbPath = CONSTANTS.FOLDER_DATA_USERS + "/" + this.pathName + "/" + this.pathName + ".db";
        this.userSettingsPath = CONSTANTS.FOLDER_DATA_USERS + "/" + this.pathName + "_settings.json";
        this.appSettingsPath = CONSTANTS.FOLDER_DATA_USERS + "/" + this.pathName + "_app_settings.json";
        this.attachmentsFolderPath = CONSTANTS.FOLDER_DATA_USERS + "/" + this.pathName + "/" + CONSTANTS.USER_ATTACHMENT_FOLDER_NAME;
    }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public void setCreated(String created) { this.created = LocalDateTime.parse(created, dateTimeFormatter); }
    public void setCreated() { this.created = LocalDateTime.now(); }
    public void setLoginSessions(int loginSessions) { this.loginSessions = loginSessions; }
    public void setLoginDurationSeconds(int loginDurationSeconds) { this.loginDurationSeconds = loginDurationSeconds; }
    public void setIsLoggedIn(boolean isLoggedIn) { this.isLoggedIn = isLoggedIn; }


}

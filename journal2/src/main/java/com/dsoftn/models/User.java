package com.dsoftn.models;

import java.time.LocalDateTime;
import java.time.Duration;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import com.dsoftn.utils.LanguagesEnum;
import com.dsoftn.utils.UFile;
import com.dsoftn.utils.UJson;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.utils.UError;
import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.services.SQLiteDB;


public class User {
    // Constants
    private final String PATH_NAME_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private final String USERNAME_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private final int USERNAME_MAX_LENGTH = 100;
    private final String PASSWORD_ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()~-_=+[]{}|;:,.<>/?'\"";
    private final Character PASSWORD_END_CHAR = '`';
    private final int PASSWORD_MAX_LENGTH = 100;
    private final int PASSWORD_STEP = 3;

    // Properties
    // Mandatory
    private String username = "";
    private LanguagesEnum language = LanguagesEnum.ENGLISH;
    private String password = "";
    private String passwordString1 = "";
    private String passwordString2 = "";
    private String passwordString3 = "";
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
    private LocalDateTime lastSessionStart = LocalDateTime.now();
    private boolean isLoggedIn = false;
    private boolean multipleLoginAllowed = OBJECTS.SETTINGS.getvBOOLEAN("AllowMultipleLogins");


    // Public methods

    public boolean load() {
        if (userInfoPath == null || userInfoPath.isEmpty()) return false;
        return load(userInfoPath);
    }

    public boolean load(String userInfoFilePath) {
        if (userInfoFilePath == null || userInfoFilePath.isEmpty()) return false;
        // Add relative path if just file name is passed
        String baseFileName = UFile.getBaseFileName(userInfoFilePath);
        String baseFolder = UFile.getBaseFolder(userInfoFilePath);
        if (baseFolder.isEmpty()) {
            if (baseFileName.endsWith("_info.json")) {
                baseFolder = UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, baseFileName.substring(0, baseFileName.indexOf("_info.json")));
            }
            else {
                baseFolder = CONSTANTS.FOLDER_DATA_USERS;
            }
        }
        // If just folder name is passed
        if (!userInfoFilePath.endsWith("_info.json")) {
            String possibleUserInfoFilePath;
            possibleUserInfoFilePath = UFile.concatPaths(baseFolder, baseFileName, baseFileName + "_info.json");
            
            if (UFile.isFile(possibleUserInfoFilePath)) {
                baseFileName = UFile.getBaseFileName(possibleUserInfoFilePath);
                baseFolder = UFile.getBaseFolder(possibleUserInfoFilePath);
            }
        }
        // Final userInfoFilePath
        userInfoFilePath = UFile.concatPaths(baseFolder, baseFileName);
        
        // Check if file exists
        if (!UFile.isFile(userInfoFilePath)) return false;
        // Get user info content
        Map<String, String> userInfoContent = getUserInfoContent(userInfoFilePath);
        if (userInfoContent == null) return false;
        // Check if content has all mandatory fields
        if (!userInfoContent.containsKey("UserName")
            || !userInfoContent.containsKey("PathName")
            || !userInfoContent.containsKey("Language")
            || !userInfoContent.containsKey("Password")
            || !userInfoContent.containsKey("Data")
            || !userInfoContent.containsKey("Settings")) return false;

        Map<String, Object> userInfoMap = new LinkedHashMap<>();
        userInfoMap.putAll(userInfoContent);
        fromMap(userInfoMap);

        if (!hasValidFileStructure()) {
            repairFileStructure();
            UError.info("USER: " + username, "File structure has been repaired");
        }

        return true;
    }

    public boolean saveUserInfoFile() { return saveUserInfoFile(userInfoPath); }

    public boolean saveUserInfoFile(String userInfoFilePath) {
        if (userInfoFilePath == null || userInfoFilePath.isEmpty()) return false;

        try {
            UJson.saveJsonFile(userInfoFilePath, toMap());
            return true;
        }
        catch (Exception e) {
            UError.exception("USER: Failed to save user info file: " + userInfoFilePath, e);
            return false;
        }
    }

    public boolean add() {
        // Set pathName
        int counter = 1;
        String validPath = getNewPathName(username);
        pathName = validPath;
        while (true) {
            if (!canBePathname(pathName)) {
                pathName = validPath + "_" + counter;
                counter++;
            }
            else {
                break;
            }
        }
        if (!setPathName(pathName)) return false;

        // Make main user folder
        boolean makeMainUserFolder = UFile.createFolder(UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, pathName));
        if (!makeMainUserFolder) return false;

        // Add to repository
        boolean addToRepository = OBJECTS.USERS.add(this);
        if (!addToRepository) return false;

        // Save UserInfo file
        boolean saveToDisk = saveUserInfoFile();
        if (!saveToDisk) {
            OBJECTS.USERS.delete(this);
            return false;
        }

        // Create database file
        SQLiteDB db = new SQLiteDB(dbPath);
        boolean createDatabase = db.constructAllTables();
        if (!createDatabase) {
            UError.error("USER: " + username, "Failed to create database");
            OBJECTS.USERS.delete(this);
            db.disconnect();
            return false;
        }

        db.disconnect();

        repairFileStructure();
        return true;
    }

    public boolean update() {
        if (!hasValidFileStructure()) {
            repairFileStructure();
        };

        return OBJECTS.USERS.update(this);
    }

    public boolean delete() { return OBJECTS.USERS.delete(this); }

    public int getSessionElapsedSeconds() { return (int) Duration.between(lastSessionStart, LocalDateTime.now()).getSeconds(); }

    public boolean isUserNameValid(String userName) {
        if (userName == null || userName.isEmpty()) return false;

        if (userName.length() > USERNAME_MAX_LENGTH) return false;
        
        boolean valid = true;
        for (int i = 0; i < userName.length(); i++) {
            String c = userName.substring(i, i + 1);
            if (!USERNAME_ALLOWED_CHARS.contains(c)) {
                valid = false;
                break;
            }
        }

        return valid;
    }

    public boolean isPasswordValid(String password) {
        if (password == null) return false;

        if (password.length() > PASSWORD_MAX_LENGTH) return false;
        
        boolean valid = true;
        for (int i = 0; i < password.length(); i++) {
            String c = password.substring(i, i + 1);
            if (!PASSWORD_ALLOWED_CHARS.contains(c)) {
                valid = false;
                break;
            }
        }

        return valid;
    }

    public String getUserNameAllowedChars() { return USERNAME_ALLOWED_CHARS; }

    public int getUserNameMaxLength() { return USERNAME_MAX_LENGTH; }

    public String getPasswordAllowedChars() { return PASSWORD_ALLOWED_CHARS; }

    public int getPasswordMaxLength() { return PASSWORD_MAX_LENGTH; }

    // Serialize / Deserialize

    public void fromMap(Map<String, Object> map) {
        username = (String) map.get("UserName") != null ? (String) map.get("UserName") : "";
        
        pathName = (String) map.get("PathName") != null ? (String) map.get("PathName") : "";
        setPathName(pathName);
        
        String languageCode = (String) map.get("Language") != null ? (String) map.get("Language") : "";
        language = LanguagesEnum.fromLangCode(languageCode);
        
        passwordString1 = (String) map.get("Password") != null ? (String) map.get("Password") : "";
        passwordString2 = (String) map.get("Data") != null ? (String) map.get("Data") : "";
        passwordString3 = (String) map.get("Settings") != null ? (String) map.get("Settings") : "";
        password = decryptPassword(passwordString1, passwordString2, passwordString3);
        
        name = (String) map.get("Name") != null ? (String) map.get("Name") : "";
        email = (String) map.get("Email") != null ? (String) map.get("Email") : "";
        phone = (String) map.get("Phone") != null ? (String) map.get("Phone") : "";
        address = (String) map.get("Address") != null ? (String) map.get("Address") : "";
        notes = (String) map.get("Notes") != null ? (String) map.get("Notes") : "";
        
        try {
            LocalDateTime creationTime = LocalDateTime.parse((String) map.get("CreationTime"), CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
            created = creationTime;
        } catch (Exception e) {
            UError.warning("USER: Failed to parse creation time", e.getMessage());
            created = LocalDateTime.now();
        }

        Integer parsedLoginSessions = UNumbers.toInteger((String) map.get("LoginSessions"));
        loginSessions = parsedLoginSessions != null ? parsedLoginSessions : 0;
        Integer parsedLoginDurationSeconds = UNumbers.toInteger((String) map.get("LoginDurationSeconds"));
        loginDurationSeconds = parsedLoginDurationSeconds != null ? parsedLoginDurationSeconds : 0;
        
        try {
            LocalDateTime lastSessionStart = LocalDateTime.parse((String) map.get("LastSessionStart"), CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
            this.lastSessionStart = lastSessionStart;
        } catch (Exception e) {
            UError.warning("USER: Failed to parse last session start time", e.getMessage());
            this.lastSessionStart = LocalDateTime.now();
        }
        
        String isLoggedInString = (String) map.get("IsLoggedIn") != null ? (String) map.get("IsLoggedIn") : "false";
        if (isLoggedInString.equalsIgnoreCase("true")) {
            isLoggedIn = true;
        }
        else {
            isLoggedIn = false;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();        
        map.put("UserName", username);
        map.put("PathName", pathName);
        map.put("Language", language.getLangCode());
        map.put("Password", passwordString1);
        map.put("Data", passwordString2);
        map.put("Settings", passwordString3);
        map.put("Name", name);
        map.put("Email", email);
        map.put("Phone", phone);
        map.put("Address", address);
        map.put("Notes", notes);
        map.put("CreationTime", created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON));
        map.put("LoginSessions", loginSessions);
        map.put("LastSessionStart", lastSessionStart.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON));
        map.put("LoginDurationSeconds", loginDurationSeconds);
        map.put("IsLoggedIn", isLoggedIn);
        return map;
    }

    // Private methods

    private String getNewPathName(String userName) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < userName.length(); i++) {
            String c = userName.substring(i, i + 1);
            
            if (PATH_NAME_ALLOWED_CHARS.contains(c)) {
                sb.append(c);
            }
            else {
                sb.append("_");
            }
        }

        String result = sb.toString();
        if (result.startsWith("_")) result = "U" + result.substring(1);

        return result;
    }

    private boolean canBePathname(String pathName) {
        List<User> users = OBJECTS.USERS.getEntityAll();
        for (int i = 0; i < users.size(); i++) {
            String userPathName = users.get(i).getPathName();
            if (userPathName != null && !userPathName.isEmpty()) {
                userPathName = userPathName.toLowerCase();
                if (pathName.toLowerCase().equals(userPathName)) {
                    pathName = userPathName;
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasValidFileStructure() {
        if (pathName == null || pathName.isEmpty()) return false;

        boolean result = true;

        if (dbPath == null || !UFile.isFile(dbPath)) result = false;
        if (userSettingsPath == null || !UFile.isFile(userSettingsPath)) result = false;
        if (appSettingsPath == null || !UFile.isFile(appSettingsPath)) result = false;
        if (attachmentsFolderPath == null || !UFile.isDirectory(attachmentsFolderPath)) result = false;

        return result;
    }

    private void repairFileStructure() {
        if (pathName == null || pathName.isEmpty()) return;

        if (dbPath == null || !UFile.isFile(dbPath)) {
            UFile.createFile(dbPath);
        }
        if (userSettingsPath == null || !UFile.isFile(userSettingsPath)) {
            UJson.saveJsonFile(userSettingsPath, new LinkedHashMap<>());
        }
        if (appSettingsPath == null || !UFile.isFile(appSettingsPath)) {
            UJson.saveJsonFile(appSettingsPath, new LinkedHashMap<>());
        }
        if (attachmentsFolderPath == null || !UFile.isDirectory(attachmentsFolderPath)) {
            UFile.createFolder(attachmentsFolderPath);
        }
    }

    private String decryptPassword(String passwordStr1, String passwordStr2, String passwordStr3) {
        if (passwordStr1.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        int pos = PASSWORD_STEP;
        while (true) {
            // First string
            if (pos >= passwordStr1.length()) {
                UError.error("USER: Failed to decrypt password", "Password string1 is too short");
                return null;
            }
            char c1 = passwordStr1.charAt(pos);
            if (c1 == PASSWORD_END_CHAR) break;
            sb.append(c1);
            // Second string
            if (pos >= passwordStr2.length()) {
                UError.error("USER: Failed to decrypt password", "Password string2 is too short");
                return null;
            }
            char c2 = passwordStr2.charAt(pos);
            if (c2 == PASSWORD_END_CHAR) break;
            sb.append(c2);
            // Third string
            if (pos >= passwordStr3.length()) {
                UError.error("USER: Failed to decrypt password", "Password string3 is too short");
            }
            char c3 = passwordStr3.charAt(pos);
            if (c3 == PASSWORD_END_CHAR) break;
            sb.append(c3);

            pos += PASSWORD_STEP;
        }

        return sb.toString();
    }

    private List<String> encryptPassword(String password) {
        List<String> passwordStringsList = new ArrayList<>();
        
        String string1 = getRandomPasswordString();
        String string2 = getRandomPasswordString();
        String string3 = getRandomPasswordString();

        if (password == null || password.isEmpty()) {
            passwordStringsList.add("");
            passwordStringsList.add(string2);
            passwordStringsList.add(string3);
            return passwordStringsList;
        }

        int pos = PASSWORD_STEP;
        int stringLine = 0;

        for (int i = 0; i <= password.length(); i++) {
            char ch;

            if (i == password.length()) {
                ch = PASSWORD_END_CHAR;
            }
            else {
                ch = password.charAt(i);
            }

            String c = String.valueOf(ch);

            if (stringLine == 0) {
                string1 = string1.substring(0, pos) + c + string1.substring(pos + 1);
                stringLine = 1;
            } else if (stringLine == 1) {
                string2 = string2.substring(0, pos) + c + string2.substring(pos + 1);
                stringLine = 2;
            } else if (stringLine == 2) {
                string3 = string3.substring(0, pos) + c + string3.substring(pos + 1);
                stringLine = 0;
                pos += PASSWORD_STEP;
            }
        }

        passwordStringsList.add(string1);
        passwordStringsList.add(string2);
        passwordStringsList.add(string3);
        return passwordStringsList;
    }

    private String getRandomPasswordString() {
        int minLength = (int) (PASSWORD_MAX_LENGTH / 3 + 2) * PASSWORD_STEP;
        int maxLength = (int) minLength + PASSWORD_STEP * 20;
        int length = (int) (Math.random() * (maxLength - minLength + 1)) + minLength;
        return getRandomString(length, PASSWORD_ALLOWED_CHARS);
    }

    private Map<String, String> getUserInfoContent(String userInfoFilePath) {
        Map<String, Object> userInfoMap = UJson.loadJsonFileToMap(userInfoFilePath);
        if (userInfoMap == null) return null;
        // Convert to Map<String, String>
        Map<String, String> userInfoContent = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : userInfoMap.entrySet()) {
            userInfoContent.put(entry.getKey(), entry.getValue().toString());
        }
        return userInfoContent;
    }

    private String getRandomString(int length, String allowedCharacters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * allowedCharacters.length());
            sb.append(allowedCharacters.charAt(randomIndex));
        }
        return sb.toString();
    }

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
    public LocalDateTime getCreated() { return created; }
    public String getCreatedString() { return created.format(CONSTANTS.DATE_TIME_FORMATTER); }
    public String getCreatedForJsonString() { return created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON); }
    public int getLoginSessions() { return loginSessions; }
    public int getLoginDurationSeconds() { return loginDurationSeconds; }
    public LocalDateTime getLastSessionStart() { return lastSessionStart; }
    public String getLastSessionStartString() { return lastSessionStart.format(CONSTANTS.DATE_TIME_FORMATTER); }
    public boolean getIsLoggedIn() { return isLoggedIn; }
    public boolean getMultipleLoginAllowed() { return multipleLoginAllowed; }

    // Setters
    public void setUsername(String username) {
        if (username == null) {
            username = "";
        }

        if (username.length() > USERNAME_MAX_LENGTH) {
            this.username = null;
            UError.error("USER: Failed to set username", "Username is too long");
            return;
        }

        if (!isUserNameValid(username)) {
            this.username = null;
            UError.error("USER: Failed to set username", "Username is invalid");
            return;
        }
        
        this.username = username;
    }
    public void setLanguage(LanguagesEnum language) { this.language = language; }
    public void setPassword(String password) {
        if (password == null) {
            password = "";
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
            this.password = null;
            UError.error("USER: Failed to set password", "Password is too long");
            return;
        }

        if (!isPasswordValid(password)) {
            this.password = null;
            UError.error("USER: Failed to set password", "Password is invalid");
            return;
        }

        List<String> passwordStringsList = encryptPassword(password);
        this.passwordString1 = passwordStringsList.get(0);
        this.passwordString2 = passwordStringsList.get(1);
        this.passwordString3 = passwordStringsList.get(2);

        if (password.isEmpty()) {
            this.password = "";
            return;
        }
        
        this.password = password;
    }

    public boolean setPathName(String pathName) {
        if (pathName == null || pathName.isEmpty()) {
            this.pathName = "";
            this.userInfoPath = "";
            this.dbPath = "";
            this.userSettingsPath = "";
            this.appSettingsPath = "";
            this.attachmentsFolderPath = "";
            return false;
        }

        this.pathName = pathName;
        this.userInfoPath = UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, this.pathName, this.pathName + "_info.json");
        this.dbPath = UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, this.pathName, this.pathName + ".db");
        this.userSettingsPath = UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, this.pathName, this.pathName + "_settings.json");
        this.appSettingsPath = UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, this.pathName, this.pathName + "_app_settings.json");
        this.attachmentsFolderPath = UFile.concatPaths(CONSTANTS.FOLDER_DATA_USERS, this.pathName, CONSTANTS.USER_ATTACHMENT_FOLDER_NAME);

        return true;
    }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public void setCreated(String created) { this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER); }
    public void setCreated() { this.created = LocalDateTime.now(); }
    public void setLoginSessions(int loginSessions) { this.loginSessions = loginSessions; }
    public void setLastSessionStart(String lastSessionStart) { this.lastSessionStart = LocalDateTime.parse(lastSessionStart, CONSTANTS.DATE_TIME_FORMATTER); }
    public void setLastSessionStart(LocalDateTime lastSessionStart) { this.lastSessionStart = lastSessionStart; }
    public void setLastSessionStart() { this.lastSessionStart = LocalDateTime.now(); }
    public void setLoginDurationSeconds(int loginDurationSeconds) { this.loginDurationSeconds = loginDurationSeconds; }
    public void setIsLoggedIn(boolean isLoggedIn) { this.isLoggedIn = isLoggedIn; }


}

package com.dsoftn;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.dsoftn.controllers.MainWinController;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.services.Clip;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.services.timer.GlobalTimer;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UFile;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.SplashScreenController;

import java.io.File;

import com.dsoftn.controllers.LoginController;

public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Check directory if structure exists, if not, create it
        createDirectoryStructure();
        
        // If settings or languages files do not exist, exit program with error message
        if (!isSettingsAndLanguageFilesExist()) {
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText("Configuration Error");
            msgBoxController.setHeaderText("Missing Settings and/or Language File");
            msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
            msgBoxController.setContentText("The settings and/or language file is required for the application to run.\nPlease copy the settings and/or language file in 'data/app/settings' and try again.\nOr reinstall the application.");
            msgBoxController.setContentIcon(MsgBoxIcon.FILE_ERROR);
            msgBoxController.setButtons(MsgBoxButton.OK);
            msgBoxController.setDefaultButton(MsgBoxButton.OK);
            msgBoxController.startMe();

            Platform.exit();
            return;
        }

        // Load settings
        OBJECTS.SETTINGS.userSettingsFilePath = CONSTANTS.SETTINGS_FILE_PATH;
        OBJECTS.SETTINGS.languagesFilePath = CONSTANTS.LANGUAGES_FILE_PATH;
        OBJECTS.SETTINGS.setActiveLanguage(CONSTANTS.DEFAULT_SETTINGS_LANGUAGE_CODE);
        OBJECTS.SETTINGS.load(true, true, false);

        // Set Users object
        OBJECTS.USERS.load();
        
        // Start Login Window
        Stage loginStage = new Stage();
        LoginController loginController = startLogin(loginStage);

        // Check if user is authenticated
        if (loginController.getAuthenticatedUser() == null) {
            Platform.exit();
            return;
        }

        // Create active user
        OBJECTS.ACTIVE_USER = loginController.getAuthenticatedUser();
        OBJECTS.ACTIVE_USER.setIsLoggedIn(true);
        OBJECTS.ACTIVE_USER.setLastSessionStart();
        OBJECTS.ACTIVE_USER.saveUserInfoFile();

        // Setup settings for active user
        OBJECTS.SETTINGS.reset();
        OBJECTS.SETTINGS.defaultSettingsFilePath = CONSTANTS.SETTINGS_FILE_PATH;
        OBJECTS.SETTINGS.languagesFilePath = CONSTANTS.LANGUAGES_FILE_PATH;
        OBJECTS.SETTINGS.userSettingsFilePath = OBJECTS.ACTIVE_USER.getUserSettingsPath();
        OBJECTS.SETTINGS.appDataFilePath = OBJECTS.ACTIVE_USER.getAppSettingsPath();
        OBJECTS.SETTINGS.setActiveLanguage(OBJECTS.ACTIVE_USER.getLanguage().getLangCode());
        OBJECTS.SETTINGS.clearErrorString();
        boolean loadUserSettingsSuccess = OBJECTS.SETTINGS.load();
        if (!loadUserSettingsSuccess) {
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText("Configuration Error");
            msgBoxController.setHeaderText("Error Loading User Settings");
            msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
            msgBoxController.setContentText("An error occurred while loading the user settings file.\nApplication will now exit.\n\n" + OBJECTS.SETTINGS.getLastErrorString());
            msgBoxController.setContentIcon(MsgBoxIcon.FILE_ERROR);
            msgBoxController.setButtons(MsgBoxButton.OK);
            msgBoxController.setDefaultButton(MsgBoxButton.OK);
            msgBoxController.startMe();

            Platform.exit();
            return;
        }

        // Create database connection
        OBJECTS.DATABASE = new SQLiteDB();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OBJECTS.DATABASE.disconnect();
        }));

        if (!OBJECTS.DATABASE.isConnected()) {
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText("Configuration Error");
            msgBoxController.setHeaderText("Error Connecting to Database");
            msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
            msgBoxController.setContentText("An error occurred while connecting to the database.\nApplication will now exit.");
            msgBoxController.setContentIcon(MsgBoxIcon.DATABASE);
            msgBoxController.setButtons(MsgBoxButton.OK);
            msgBoxController.setDefaultButton(MsgBoxButton.OK);
            msgBoxController.startMe();

            Platform.exit();
            return;
        }

        // Create GlobalTimer in OBJECTS
        OBJECTS.GLOBAL_TIMER = new GlobalTimer();
        // Create Clip in OBJECTS
        OBJECTS.CLIP = new Clip();

        loginStage.close();

        // Show Splash Screen while models is loading
        SplashScreenController splashScreenController = DIALOGS.getSplashScreenController(null);
        splashScreenController.startMe();

        // If models are not loaded, exit program with error message
        if (getErrorsInModelLoading() != "") {
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText("Configuration Error");
            msgBoxController.setHeaderText("Error Loading Models");
            msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
            msgBoxController.setContentText("An error occurred while loading the models.\nApplication will now exit.\n\n" + getErrorsInModelLoading());
            msgBoxController.setContentIcon(MsgBoxIcon.MODEL);
            msgBoxController.setButtons(MsgBoxButton.OK);
            msgBoxController.setDefaultButton(MsgBoxButton.OK);
            msgBoxController.startMe();

            Platform.exit();
            return;
        }

        // Destroy splash screen and login window
        loginStage = null;
        splashScreenController = null;

        // Start Main Window
        startMainWin(primaryStage);
    }

    private String getErrorsInModelLoading() {
        String errors = "";

        if (!OBJECTS.RELATIONS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Relations are not properly loaded");
            errors += "Relations are not properly loaded\n";
        }

        if (!OBJECTS.DEFINITIONS_VARIANTS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Definition Variants are not properly loaded");
            errors += "Definition Variants are not properly loaded\n";
        }

        if (!OBJECTS.TAGS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Tags are not properly loaded");
            errors += "Tags are not properly loaded\n";
        }

        if (!OBJECTS.CATEGORIES.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Categories are not properly loaded");
            errors += "Categories are not properly loaded\n";
        }

        if (!OBJECTS.ATTACHMENTS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Attachments are not properly loaded");
            errors += "Attachments are not properly loaded\n";
        }

        if (!OBJECTS.ACTORS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Actors are not properly loaded");
            errors += "Actors are not properly loaded\n";
        }

        if (!OBJECTS.BLOCKS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Blocks are not properly loaded");
            errors += "Blocks are not properly loaded\n";
        }

        if (!OBJECTS.BLOCKS_DIARY.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Blocks Diary are not properly loaded");
            errors += "Blocks Diary are not properly loaded\n";
        }

        if (!OBJECTS.DEFINITIONS.isModelLoaded()) {
            UError.error("GuiMain.getErrorsInModelLoading: Definitions are not properly loaded");
            errors += "Definitions are not properly loaded\n";
        }

        return errors;
    }

    private LoginController startLogin(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle(CONSTANTS.APPLICATION_NAME);

        controller.setStage(primaryStage);

        controller.startMe();

        return controller;
    }

    private void startMainWin(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWin.fxml"));
        Parent root = loader.load();

        MainWinController controller = loader.getController();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            onWindowClose(event, controller);
        });

        controller.setStage(primaryStage);

        controller.startMe();
    }

    private void createDirectoryStructure() {
        // Check if directory "data" exists, if not, create it
        File dataDir = new File(CONSTANTS.FOLDER_DATA);
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            dataDir.mkdir();
        }

        // Check if directory "data/app" exists, if not, create it
        File appDir = new File(CONSTANTS.FOLDER_DATA_APP);
        if (!appDir.exists() || !appDir.isDirectory()) {
            appDir.mkdir();
        }

        // Check if directory "data/app/settings" exists, if not, create it
        File settingsDir = new File(CONSTANTS.FOLDER_DATA_APP_SETTINGS);
        if (!settingsDir.exists() || !settingsDir.isDirectory()) {
            settingsDir.mkdir();
        }

        // Check if directory "data/users" exists, if not, create it
        File usersDir = new File(CONSTANTS.FOLDER_DATA_USERS);
        if (!usersDir.exists() || !usersDir.isDirectory()) {
            usersDir.mkdir();
        };
    }

    private boolean isSettingsAndLanguageFilesExist() {
        // Check if file "/data/app/settings/settings.json" exists
        if (!UFile.isFile(CONSTANTS.SETTINGS_FILE_PATH)) {
            // Let user find file
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText("Required File");
            msgBoxController.setHeaderText("Missing Settings File");
            msgBoxController.setHeaderIcon(MsgBoxIcon.FILE_ERROR);
            msgBoxController.setContentText("The settings file is required for the application to run.\nPlease find the settings file and try again.");
            msgBoxController.setContentIcon(MsgBoxIcon.FILE_SEARCH);
            msgBoxController.setButtons(MsgBoxButton.FIND_FILE, MsgBoxButton.CANCEL);
            msgBoxController.setDefaultButton(MsgBoxButton.FIND_FILE);
            msgBoxController.startMe();
            
            // If user did not find file, exit program
            if (msgBoxController.getSelectedButton() != MsgBoxButton.FIND_FILE || msgBoxController.getResult().isEmpty()) {
                return false;
            }

            // If user found file, copy it to "/data/app/settings/settings.json"
            boolean success = UFile.copyFile(msgBoxController.getResult(), CONSTANTS.SETTINGS_FILE_PATH);
            if (!success) {
                MsgBoxController msgBoxFailed = DIALOGS.getMsgBoxController(null);
                msgBoxFailed.setTitleText("Error");
                msgBoxFailed.setHeaderText("Failed to Copy File");
                msgBoxFailed.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxFailed.setContentText("Failed to copy settings file.\nApplication will now exit.");
                msgBoxFailed.setContentIcon(MsgBoxIcon.COPY);
                msgBoxFailed.setButtons(MsgBoxButton.OK);
                msgBoxFailed.setDefaultButton(MsgBoxButton.OK);
                msgBoxFailed.startMe();
                return false;
            }

            // Check if file "/data/app/settings/settings.json" is valid
            OBJECTS.SETTINGS.clearErrorString();
            OBJECTS.SETTINGS.userSettingsFilePath = CONSTANTS.SETTINGS_FILE_PATH;
            boolean loadSuccess = OBJECTS.SETTINGS.load(true, false, false);
            if (!loadSuccess || !OBJECTS.SETTINGS.getLastErrorString().isEmpty()) {
                MsgBoxController msgBoxFailed = DIALOGS.getMsgBoxController(null);
                msgBoxFailed.setTitleText("Error");
                msgBoxFailed.setHeaderText("Corrupted Settings File");
                msgBoxFailed.setHeaderIcon(MsgBoxIcon.ERROR);
                String msgContent = "Failed to load settings file.\nSelected file may be corrupted or invalid.";
                if (!OBJECTS.SETTINGS.getLastErrorString().isEmpty()) {
                    msgContent += "\n\nSettings object reported error:\n" + OBJECTS.SETTINGS.getLastErrorString();
                }
                msgBoxFailed.setContentText(msgContent);
                msgBoxFailed.setContentIcon(MsgBoxIcon.FILE_ERROR);
                msgBoxFailed.setButtons(MsgBoxButton.OK);
                msgBoxFailed.setDefaultButton(MsgBoxButton.OK);
                msgBoxFailed.startMe();
                // Delete corrupted file
                UFile.deleteFile(CONSTANTS.SETTINGS_FILE_PATH);
                return false;
            }

            // Reset settings to clear new file data. Settings object will be properly loaded later
            OBJECTS.SETTINGS.reset();
        }


        // Check if file "/data/app/settings/languages.json" exists
        if (!UFile.isFile(CONSTANTS.LANGUAGES_FILE_PATH)) {
            // Let user find file
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText("Required File");
            msgBoxController.setHeaderText("Missing Languages File");
            msgBoxController.setHeaderIcon(MsgBoxIcon.FILE_ERROR);
            msgBoxController.setContentText("The languages file is required for the application to run.\nPlease find the languages file and try again.");
            msgBoxController.setContentIcon(MsgBoxIcon.FILE_SEARCH);
            msgBoxController.setButtons(MsgBoxButton.FIND_FILE, MsgBoxButton.CANCEL);
            msgBoxController.setDefaultButton(MsgBoxButton.FIND_FILE);
            msgBoxController.startMe();
            
            // If user did not find file, exit program
            if (msgBoxController.getSelectedButton() != MsgBoxButton.FIND_FILE || msgBoxController.getResult().isEmpty()) {
                return false;
            }

            // If user found file, copy it to "/data/app/settings/languages.json"
            boolean success = UFile.copyFile(msgBoxController.getResult(), CONSTANTS.LANGUAGES_FILE_PATH);
            if (!success) {
                MsgBoxController msgBoxFailed = DIALOGS.getMsgBoxController(null);
                msgBoxFailed.setTitleText("Error");
                msgBoxFailed.setHeaderText("Failed to Copy File");
                msgBoxFailed.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxFailed.setContentText("Failed to copy languages file.\nApplication will now exit.");
                msgBoxFailed.setContentIcon(MsgBoxIcon.COPY);
                msgBoxFailed.setButtons(MsgBoxButton.OK);
                msgBoxFailed.setDefaultButton(MsgBoxButton.OK);
                msgBoxFailed.startMe();
                return false;
            }

            // Check if file "/data/app/settings/languages.json" is valid
            OBJECTS.SETTINGS.clearErrorString();
            OBJECTS.SETTINGS.languagesFilePath = CONSTANTS.LANGUAGES_FILE_PATH;
            boolean loadSuccess = OBJECTS.SETTINGS.load(false, true, false);
            if (!loadSuccess || !OBJECTS.SETTINGS.getLastErrorString().isEmpty()) {
                MsgBoxController msgBoxFailed = DIALOGS.getMsgBoxController(null);
                msgBoxFailed.setTitleText("Error");
                msgBoxFailed.setHeaderText("Corrupted Languages File");
                msgBoxFailed.setHeaderIcon(MsgBoxIcon.ERROR);
                String msgContent  = "Failed to load languages file.\nSelected file may be corrupted or invalid.";
                if (!OBJECTS.SETTINGS.getLastErrorString().isEmpty()) {
                    msgContent += "\n\nSettings object reported error:\n" + OBJECTS.SETTINGS.getLastErrorString();
                }
                msgBoxFailed.setContentText(msgContent);
                msgBoxFailed.setContentIcon(MsgBoxIcon.FILE_ERROR);
                msgBoxFailed.setButtons(MsgBoxButton.OK);
                msgBoxFailed.setDefaultButton(MsgBoxButton.OK);
                msgBoxFailed.startMe();
                // Delete corrupted file
                UFile.deleteFile(CONSTANTS.LANGUAGES_FILE_PATH);
                return false;
            }
        }

        return true;
    }

    private void onWindowClose(WindowEvent event, MainWinController controller) {
        // Set active user login duration and login status. Save Active User
        OBJECTS.ACTIVE_USER.setLoginDurationSeconds(OBJECTS.ACTIVE_USER.getLoginDurationSeconds() + OBJECTS.ACTIVE_USER.getSessionElapsedSeconds());
        OBJECTS.ACTIVE_USER.setLoginSessions(OBJECTS.ACTIVE_USER.getLoginSessions() + 1);
        OBJECTS.ACTIVE_USER.setIsLoggedIn(false);
        OBJECTS.ACTIVE_USER.saveUserInfoFile();

        // Save Settings
        boolean success;
        success = OBJECTS.SETTINGS.save();
        if (!success) {
            MsgBoxController msgBoxFailed = DIALOGS.getMsgBoxController(null);
            msgBoxFailed.setTitleText("Settings");
            msgBoxFailed.setHeaderText("Failed to Save Settings");
            msgBoxFailed.setHeaderIcon(MsgBoxIcon.ERROR);
            msgBoxFailed.setContentText("An error occurred while saving settings.\nApplication will now exit.\n\n" + OBJECTS.SETTINGS.getLastErrorString());
            msgBoxFailed.setContentIcon(MsgBoxIcon.WARNING);
            msgBoxFailed.setButtons(MsgBoxButton.OK);
            msgBoxFailed.setDefaultButton(MsgBoxButton.OK);
            msgBoxFailed.startMe();
            return;
        }

        // Exit program
    }
    
}

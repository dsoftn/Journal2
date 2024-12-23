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
import com.dsoftn.utils.UFile;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;

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
        
        // Start Login Window
        Stage loginStage = new Stage();
        LoginController loginController = startLogin(loginStage);

        // Check if user is authenticated
        if (loginController.getAuthenticatedUser() == null) {
            Platform.exit();
            return;
        }

        loginStage.close();

        // Start Main Window
        startMainWin(primaryStage);
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
        if (!UFile.isFile("data/app/settings/settings.json")) {
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
            boolean success = UFile.copyFile(msgBoxController.getResult(), "data/app/settings/settings.json");
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
            OBJECTS.SETTINGS.userSettingsFilePath = "data/app/settings/settings.json";
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
                UFile.deleteFile("data/app/settings/settings.json");
                return false;
            }

            // Reset settings to clear new file data. Settings object will be properly loaded later
            OBJECTS.SETTINGS.reset();
        }


        // Check if file "/data/app/settings/languages.json" exists
        if (!UFile.isFile("data/app/settings/languages.json")) {
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
            boolean success = UFile.copyFile(msgBoxController.getResult(), "data/app/settings/languages.json");
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
            OBJECTS.SETTINGS.languagesFilePath = "data/app/settings/languages.json";
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
                UFile.deleteFile("data/app/settings/languages.json");
                return false;
            }
        }

        return true;
    }

    private void onWindowClose(WindowEvent event, MainWinController controller) {
        // TODO: Implement saving Settings
    }
    
}

package com.dsoftn;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.dsoftn.controllers.MainWinController;

import java.io.File;

import com.dsoftn.controllers.LoginController;


public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // If settings or languages files do not exist, exit program with error message
        if (!isSettingsAndLanguageFilesExist()) {
            // TODO: Inform user that settings or languages files are needed for the application to run
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

        controller.startMe(primaryStage);

        return controller;
    }

    private void startMainWin(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWin.fxml"));
        Parent root = loader.load();

        MainWinController controller = loader.getController();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle(CONSTANTS.APPLICATION_NAME);

        primaryStage.setOnCloseRequest(event -> {
            onWindowClose(event, controller);
        });

        primaryStage.show();
    }

    private boolean isSettingsAndLanguageFilesExist() {
        // Check if directory "data" exists, if not, create it
        File dataDir = new File("data");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            dataDir.mkdir();
        }

        // Check if directory "data/app" exists, if not, create it
        File appDir = new File("data/app");
        if (!appDir.exists() || !appDir.isDirectory()) {
            appDir.mkdir();
        }

        // Check if directory "data/app/settings" exists, if not, create it
        File settingsDir = new File("data/app/settings");
        if (!settingsDir.exists() || !settingsDir.isDirectory()) {
            settingsDir.mkdir();
        }

        // Check if file "/data/app/settings/settings.json" exists, if not, create it
        File settingsFile = new File("data/app/settings/settings.json");
        if (!settingsFile.exists() || !settingsFile.isFile()) {
            // TODO: Ask user to find file, otherwise exit program with error message
        }

        // Check if file "/data/app/settings/languages.json" exists, if not, create it
        File languagesFile = new File("data/app/settings/languages.json");
        if (!languagesFile.exists() || !languagesFile.isFile()) {
            // TODO: Ask user to find file, otherwise exit program with error message
        }

        return true;
    }

    private void onWindowClose(WindowEvent event, MainWinController controller) {
        // Implement saving Settings
    }
    
}

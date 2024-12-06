package com.dsoftn;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.dsoftn.controllers.MainWinController;
import com.dsoftn.controllers.LoginController;
import com.dsoftn.Settings.CONSTANTS;


public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start Login Window
        Stage loginStage = new Stage();
        LoginController loginController = startLogin(loginStage);

        // Check if user is authenticated
        if (loginController.getAuthenticatedUser() == null) {
            Platform.exit();
            return;
        }

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

        primaryStage.showAndWait();

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

    private void onWindowClose(WindowEvent event, MainWinController controller) {
        // Implement saving Settings
    }
    
}

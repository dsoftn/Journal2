package com.dsoftn;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.dsoftn.controllers.MainWinController;
import com.dsoftn.Settings.CONSTANTS;


public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWin.fxml"));
        Parent root = loader.load();

        MainWinController controller = loader.getController();

        Scene scene = new Scene(root);

        String css = GuiMain.class.getResource("/css/main.css").toExternalForm();
        scene.getStylesheets().add(css);

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

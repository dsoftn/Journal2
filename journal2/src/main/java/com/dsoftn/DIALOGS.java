package com.dsoftn;

import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.SplashScreenController;

import com.dsoftn.utils.UError;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DIALOGS {

    public static MsgBoxController getMsgBoxController(Stage parentStage) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/MsgBox.fxml"));
        
        Parent root;
        try {
            root = loader.load();
            MsgBoxController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getMsgBoxController: Failed to load dialog", e);
            return null;
        }
    }

    public static SplashScreenController getSplashScreenController(Stage parentStage) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/SplashScreen.fxml"));
        
        Parent root;
        try {
            root = loader.load();
            SplashScreenController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getSplashScreenController: Failed to load dialog", e);
            return null;
        }
    }


}

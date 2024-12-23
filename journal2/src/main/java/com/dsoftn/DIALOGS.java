package com.dsoftn;

import com.dsoftn.controllers.MsgBoxController;

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
            e.printStackTrace();
            return null;
        }
    }


}

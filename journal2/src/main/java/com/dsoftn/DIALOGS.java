package com.dsoftn;

import com.dsoftn.controllers.MsgBoxController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DIALOGS {

    public static MsgBoxController main() {
        Stage stage = new Stage();

        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/MsgBox.fxml"));
        
        Parent root;
        try {
            root = loader.load();
            MsgBoxController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

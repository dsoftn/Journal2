package com.dsoftn;

import com.dsoftn.controllers.elements.BlockGeneralController;

import com.dsoftn.utils.UError;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class ELEMENTS {

    public static BlockGeneralController getBlockGeneralController() {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/BlockGeneral.fxml"));
        
        try {
            AnchorPane root = loader.load();
            BlockGeneralController controller = loader.getController();
            controller.setRoot(root);
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getBlockGeneralController: Failed to load element", e);
            return null;
        }
    }

}
